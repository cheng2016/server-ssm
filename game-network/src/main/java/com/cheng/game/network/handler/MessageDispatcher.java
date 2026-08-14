package com.cheng.game.network.handler;

import com.cheng.game.common.error.ErrorCode;
import com.cheng.game.common.log.LogMdc;
import com.cheng.game.network.codec.GamePacket;
import com.cheng.game.network.codec.MessageRegistry;
import com.cheng.game.network.codec.SystemNotifies;
import com.cheng.game.network.limit.GameRateLimiter;
import com.cheng.game.network.metric.GameMetrics;
import com.cheng.game.network.server.GameBusinessExecutor;
import com.cheng.game.network.session.SessionManager;
import com.cheng.game.protocol.NotifyReason;
import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;

@Component
public class MessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(MessageDispatcher.class);

    private final ApplicationContext applicationContext;
    private final SessionManager sessionManager;
    private final MessageRegistry messageRegistry;
    private final GameBusinessExecutor businessExecutor;
    private final GameRateLimiter rateLimiter;
    private final GameMetrics gameMetrics;
    private final Map<Short, HandlerMethod> handlers = new HashMap<>();

    public MessageDispatcher(ApplicationContext applicationContext,
                             SessionManager sessionManager,
                             MessageRegistry messageRegistry,
                             GameBusinessExecutor businessExecutor,
                             GameRateLimiter rateLimiter,
                             GameMetrics gameMetrics) {
        this.applicationContext = applicationContext;
        this.sessionManager = sessionManager;
        this.messageRegistry = messageRegistry;
        this.businessExecutor = businessExecutor;
        this.rateLimiter = rateLimiter;
        this.gameMetrics = gameMetrics;
    }

    @PostConstruct
    public void init() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(GameMessageController.class);
        for (Object bean : beans.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            for (Method method : targetClass.getMethods()) {
                GameHandler annotation = method.getAnnotation(GameHandler.class);
                if (annotation == null) {
                    continue;
                }
                method.setAccessible(true);
                HandlerMethod previous = handlers.put(annotation.msgId(), new HandlerMethod(bean, method));
                if (previous != null) {
                    throw new IllegalStateException("Duplicate GameHandler for msgId=" + annotation.msgId());
                }
                log.info("Registered GameHandler msgId={} -> {}.{}",
                        annotation.msgId(), targetClass.getSimpleName(), method.getName());
            }
        }
    }

    public void dispatch(ChannelHandlerContext ctx, GamePacket packet) {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        bindMdc(ctx, traceId);
        try {
            gameMetrics.recordMessage();
            if (!rateLimiter.allow(ctx, packet.msgId())) {
                gameMetrics.recordRateLimited();
                ctx.writeAndFlush(SystemNotifies.of(ErrorCode.RATE_LIMITED, NotifyReason.RATE_LIMIT));
                return;
            }
            try {
                businessExecutor.execute(() -> invokeHandler(ctx, packet, traceId));
            } catch (RejectedExecutionException e) {
                ctx.writeAndFlush(SystemNotifies.of(ErrorCode.BUSY, NotifyReason.BUSY));
            }
        } finally {
            MDC.clear();
        }
    }

    private void invokeHandler(ChannelHandlerContext ctx, GamePacket packet, String traceId) {
        bindMdc(ctx, traceId);
        HandlerMethod handler = handlers.get(packet.msgId());
        if (handler == null) {
            log.warn("No handler for msgId={} channel={}", packet.msgId(), ctx.channel().id());
            return;
        }
        try {
            Object parsed = null;
            if (messageRegistry.supports(packet.msgId())) {
                parsed = messageRegistry.parse(packet.msgId(), packet.payload());
            }
            Object result = handler.invoke(ctx, packet, parsed);
            if (result instanceof GamePacket response) {
                ctx.writeAndFlush(response);
            } else if (result instanceof MessageLite messageLite) {
                log.warn("Handler returned MessageLite without msgId wrapper: {}", messageLite.getClass().getSimpleName());
            }
            Long playerId = ctx.channel().attr(SessionManager.PLAYER_ID).get();
            if (playerId != null) {
                MDC.put(LogMdc.PLAYER_ID, String.valueOf(playerId));
                sessionManager.get(playerId).ifPresent(session -> session.touch());
            }
        } catch (Exception e) {
            log.error("Dispatch failed msgId={} channel={}", packet.msgId(), ctx.channel().id(), e);
        } finally {
            MDC.clear();
        }
    }

    private static void bindMdc(ChannelHandlerContext ctx, String traceId) {
        MDC.put(LogMdc.TRACE_ID, traceId);
        Long playerId = ctx.channel().attr(SessionManager.PLAYER_ID).get();
        if (playerId != null) {
            MDC.put(LogMdc.PLAYER_ID, String.valueOf(playerId));
        }
    }

    private record HandlerMethod(Object bean, Method method) {
        Object invoke(ChannelHandlerContext ctx, GamePacket packet, Object parsed) throws Exception {
            Class<?>[] types = method.getParameterTypes();
            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                Class<?> type = types[i];
                if (ChannelHandlerContext.class.isAssignableFrom(type)) {
                    args[i] = ctx;
                } else if (byte[].class.equals(type)) {
                    args[i] = packet.payload();
                } else if (GamePacket.class.equals(type)) {
                    args[i] = packet;
                } else if (MessageLite.class.isAssignableFrom(type)) {
                    if (parsed == null || !type.isInstance(parsed)) {
                        throw new IllegalStateException("No parsed protobuf for " + type.getName());
                    }
                    args[i] = parsed;
                } else {
                    throw new IllegalStateException("Unsupported handler param: " + type.getName());
                }
            }
            return method.invoke(bean, args);
        }
    }
}
