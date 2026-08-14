package com.cheng.game.network.server;

import com.cheng.game.network.config.NettyServerProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class NettyServerLifecycle implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(NettyServerLifecycle.class);

    private final NettyServerProperties properties;
    private final TcpChannelInitializer tcpChannelInitializer;
    private final WebSocketChannelInitializer webSocketChannelInitializer;

    private volatile boolean running;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel tcpChannel;
    private Channel wsChannel;

    public NettyServerLifecycle(NettyServerProperties properties,
                                TcpChannelInitializer tcpChannelInitializer,
                                WebSocketChannelInitializer webSocketChannelInitializer) {
        this.properties = properties;
        this.tcpChannelInitializer = tcpChannelInitializer;
        this.webSocketChannelInitializer = webSocketChannelInitializer;
    }

    @Override
    public void start() {
        if (!properties.isEnabled() || running) {
            return;
        }
        int workers = properties.getWorkerThreads() > 0
                ? properties.getWorkerThreads()
                : Math.max(4, Runtime.getRuntime().availableProcessors() * 2);
        bossGroup = new NioEventLoopGroup(properties.getBossThreads());
        workerGroup = new NioEventLoopGroup(workers);
        try {
            ServerBootstrap tcpBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(tcpChannelInitializer);
            tcpChannel = tcpBootstrap.bind(properties.getTcpPort()).sync().channel();
            log.info("Netty TCP game server started on port {}", properties.getTcpPort());

            ServerBootstrap wsBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(webSocketChannelInitializer);
            wsChannel = wsBootstrap.bind(properties.getWebsocketPort()).sync().channel();
            log.info("Netty WebSocket game server started on port {} path {}",
                    properties.getWebsocketPort(), properties.getWebsocketPath());
            running = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to start Netty servers", e);
        } catch (Exception e) {
            stop();
            throw new IllegalStateException("Failed to start Netty servers", e);
        }
    }

    @Override
    public void stop() {
        running = false;
        closeQuietly(tcpChannel);
        closeQuietly(wsChannel);
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("Netty game servers stopped");
    }

    private void closeQuietly(Channel channel) {
        if (channel != null) {
            try {
                channel.close().syncUninterruptibly();
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    public boolean isTcpBound() {
        return tcpChannel != null && tcpChannel.isActive();
    }

    public boolean isWebsocketBound() {
        return wsChannel != null && wsChannel.isActive();
    }

    public int tcpPort() {
        return properties.getTcpPort();
    }

    public int websocketPort() {
        return properties.getWebsocketPort();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 100;
    }
}
