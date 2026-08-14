package com.cheng.game.network.metric;

import com.cheng.game.network.session.SessionManager;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class GameMetrics {

    private final Counter messagesReceived;
    private final Counter rateLimited;

    public GameMetrics(ObjectProvider<MeterRegistry> registries, SessionManager sessionManager) {
        MeterRegistry registry = registries.getIfAvailable(SimpleMeterRegistry::new);
        this.messagesReceived = Counter.builder("game.messages.received")
                .description("Inbound game packets")
                .register(registry);
        this.rateLimited = Counter.builder("game.rate.limited")
                .description("Packets dropped by rate limiter")
                .register(registry);
        Gauge.builder("game.online.players", sessionManager, SessionManager::onlineCount)
                .description("Bound player sessions")
                .register(registry);
        Gauge.builder("game.connections", sessionManager, SessionManager::connectionCount)
                .description("Active TCP/WebSocket connections")
                .register(registry);
        Gauge.builder("game.kicks", sessionManager, manager -> (double) manager.kickCount())
                .description("Successful kick operations")
                .register(registry);
    }

    public void recordMessage() {
        messagesReceived.increment();
    }

    public void recordRateLimited() {
        rateLimited.increment();
    }
}
