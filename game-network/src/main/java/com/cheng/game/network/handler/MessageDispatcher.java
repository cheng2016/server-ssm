package com.cheng.game.network.handler;

import com.cheng.game.network.codec.GamePacket;
import com.cheng.game.network.session.SessionManager;
import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class MessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(MessageDispatcher.class);

    private final ApplicationContext applicationContext;
    private final SessionManager sessionManager;
    private final Map<Short, HandlerMethod> handlers = new HashMap<>();

    public MessageDispatcher(ApplicationContext applicationContext, SessionManager sessionManager) {
        this.applicationContext = applicationContext;
        this.sessionManager = sessionManager;
    }

    @PostConstruct
    public void init() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(GameMessageController.class);
        for (Object bean : beans.values()) {
            for (Method method : bean.getClass().getMethods()) {
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
                        annotation.msgId(), bean.getClass().getSimpleName(), method.getName());
            }
        }
    }

    public void dispatch(ChannelHandlerContext ctx, GamePacket packet) {
        HandlerMethod handler = handlers.get(packet.msgId());
        if (handler == null) {
            log.warn("No handler for msgId={} channel={}", packet.msgId(), ctx.channel().id());
            return;
        }
        try {
            Object result = handler.invoke(ctx, packet.payload());
            if (result instanceof GamePacket response) {
                ctx.writeAndFlush(response);
            } else if (result instanceof MessageLite messageLite) {
                log.warn("Handler returned MessageLite without msgId wrapper: {}", messageLite.getClass().getSimpleName());
            }
            Long playerId = ctx.channel().attr(SessionManager.PLAYER_ID).get();
            if (playerId != null) {
                sessionManager.get(playerId).ifPresent(session -> session.touch());
            }
        } catch (Exception e) {
            log.error("Dispatch failed msgId={} channel={}", packet.msgId(), ctx.channel().id(), e);
        }
    }

    private record HandlerMethod(Object bean, Method method) {
        Object invoke(ChannelHandlerContext ctx, byte[] payload) throws Exception {
            Class<?>[] types = method.getParameterTypes();
            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                Class<?> type = types[i];
                if (ChannelHandlerContext.class.isAssignableFrom(type)) {
                    args[i] = ctx;
                } else if (byte[].class.equals(type)) {
                    args[i] = payload;
                } else if (GamePacket.class.equals(type)) {
                    args[i] = new GamePacket((short) 0, payload);
                } else {
                    throw new IllegalStateException("Unsupported handler param: " + type.getName());
                }
            }
            return method.invoke(bean, args);
        }
    }
}
