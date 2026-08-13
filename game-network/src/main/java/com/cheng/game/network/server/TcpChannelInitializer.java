package com.cheng.game.network.server;

import com.cheng.game.network.codec.GamePacketDecoder;
import com.cheng.game.network.codec.GamePacketEncoder;
import com.cheng.game.network.config.NettyServerProperties;
import com.cheng.game.network.handler.GameServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TcpChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyServerProperties properties;
    private final GameServerHandler gameServerHandler;

    public TcpChannelInitializer(NettyServerProperties properties, GameServerHandler gameServerHandler) {
        this.properties = properties;
        this.gameServerHandler = gameServerHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast(new IdleStateHandler(properties.getReaderIdleSeconds(), 0, 0, TimeUnit.SECONDS))
                .addLast(new GamePacketDecoder())
                .addLast(new GamePacketEncoder())
                .addLast(gameServerHandler);
    }
}
