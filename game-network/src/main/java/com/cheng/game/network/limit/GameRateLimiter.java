package com.cheng.game.network.limit;

import com.cheng.game.common.protocol.MsgIds;
import com.cheng.game.network.config.NettyServerProperties;
import com.cheng.game.network.session.SessionManager;
import io.netty.channel.ChannelHandlerContext;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class GameRateLimiter {

    private final NettyServerProperties properties;
    private final WindowRateLimiter limiter = new WindowRateLimiter();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "game-rate-limit-cleaner");
        thread.setDaemon(true);
        return thread;
    });

    public GameRateLimiter(NettyServerProperties properties) {
        this.properties = properties;
        this.cleaner.scheduleAtFixedRate(() -> limiter.evictExpired(TimeUnit.MINUTES.toMillis(5)),
                1, 1, TimeUnit.MINUTES);
    }

    public boolean allow(ChannelHandlerContext ctx, short msgId) {
        String ip = clientIp(ctx);
        Long playerId = ctx.channel().attr(SessionManager.PLAYER_ID).get();
        return switch (msgId) {
            case MsgIds.HEARTBEAT_REQ -> allowKey("hb:ip:" + ip, properties.getHeartbeatLimitPerWindow(),
                    seconds(properties.getRateLimitWindowSeconds()))
                    && allowPlayer("hb:p:", playerId, properties.getHeartbeatLimitPerWindow(),
                    seconds(properties.getRateLimitWindowSeconds()));
            case MsgIds.CHAT_REQ -> allowKey("chat:ip:" + ip, properties.getChatLimitPerWindow(),
                    seconds(properties.getRateLimitWindowSeconds()))
                    && allowPlayer("chat:p:", playerId, properties.getChatLimitPerWindow(),
                    seconds(properties.getRateLimitWindowSeconds()));
            case MsgIds.LOGIN_REQ -> allowKey("login:ip:" + ip, properties.getLoginLimitPerWindow(),
                    seconds(properties.getLoginWindowSeconds()));
            default -> true;
        };
    }

    private boolean allowPlayer(String prefix, Long playerId, int limit, long windowMs) {
        if (playerId == null) {
            return true;
        }
        return allowKey(prefix + playerId, limit, windowMs);
    }

    private boolean allowKey(String key, int limit, long windowMs) {
        return limiter.tryAcquire(key, limit, windowMs);
    }

    private static long seconds(int value) {
        return TimeUnit.SECONDS.toMillis(Math.max(1, value));
    }

    public static String clientIp(ChannelHandlerContext ctx) {
        SocketAddress address = ctx.channel().remoteAddress();
        if (address instanceof InetSocketAddress inet) {
            return inet.getAddress().getHostAddress();
        }
        return "unknown";
    }

    @PreDestroy
    public void shutdown() {
        cleaner.shutdownNow();
    }
}
