package com.cheng.game.app.health;

import com.cheng.game.network.server.NettyServerLifecycle;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class NettyHealthIndicator implements HealthIndicator {

    private final NettyServerLifecycle lifecycle;

    public NettyHealthIndicator(NettyServerLifecycle lifecycle) {
        this.lifecycle = lifecycle;
    }

    @Override
    public Health health() {
        boolean tcp = lifecycle.isTcpBound();
        boolean websocket = lifecycle.isWebsocketBound();
        Health.Builder builder = (tcp && websocket) ? Health.up() : Health.down();
        return builder
                .withDetail("tcpPort", lifecycle.tcpPort())
                .withDetail("tcpBound", tcp)
                .withDetail("websocketPort", lifecycle.websocketPort())
                .withDetail("websocketBound", websocket)
                .build();
    }
}
