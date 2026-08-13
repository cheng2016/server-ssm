package com.cheng.game.network.server;

import com.cheng.game.network.config.NettyServerProperties;
import com.cheng.game.network.handler.GameServerHandler;
import com.cheng.game.network.websocket.BinaryWebSocketFrameCodec;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyServerProperties properties;
    private final GameServerHandler gameServerHandler;

    public WebSocketChannelInitializer(NettyServerProperties properties, GameServerHandler gameServerHandler) {
        this.properties = properties;
        this.gameServerHandler = gameServerHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ch.pipeline()
                .addLast(new IdleStateHandler(properties.getReaderIdleSeconds(), 0, 0, TimeUnit.SECONDS))
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new ChunkedWriteHandler())
                .addLast(new WebSocketServerProtocolHandler(properties.getWebsocketPath(), null, true))
                .addLast(new BinaryWebSocketFrameCodec())
                .addLast(gameServerHandler);
    }
}
