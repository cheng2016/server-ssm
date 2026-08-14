package com.cheng.game.network.server;

import com.cheng.game.network.config.NettyServerProperties;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GameBusinessExecutor implements Executor {

    private static final Logger log = LoggerFactory.getLogger(GameBusinessExecutor.class);

    private final ThreadPoolExecutor delegate;

    public GameBusinessExecutor(NettyServerProperties properties) {
        int threads = properties.getBusinessThreads() > 0
                ? properties.getBusinessThreads()
                : Math.max(4, Runtime.getRuntime().availableProcessors());
        int queueCapacity = Math.max(16, properties.getBusinessQueueCapacity());
        this.delegate = new ThreadPoolExecutor(
                threads,
                threads,
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                namedFactory("game-biz"),
                new ThreadPoolExecutor.AbortPolicy());
        log.info("Game business executor threads={} queue={}", threads, queueCapacity);
    }

    @Override
    public void execute(Runnable command) {
        Map<String, String> context = MDC.getCopyOfContextMap();
        delegate.execute(() -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            try {
                command.run();
            } finally {
                MDC.clear();
            }
        });
    }

    public int queueSize() {
        return delegate.getQueue().size();
    }

    @PreDestroy
    public void shutdown() {
        delegate.shutdown();
        try {
            if (!delegate.awaitTermination(5, TimeUnit.SECONDS)) {
                delegate.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            delegate.shutdownNow();
        }
    }

    private static ThreadFactory namedFactory(String prefix) {
        AtomicInteger seq = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + "-" + seq.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }
}
