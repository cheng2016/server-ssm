package com.cheng.server.netty;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

@Component
public class WebSocketChildChannelHandler extends ChannelInitializer<SocketChannel>{

    @Resource(name = "webSocketServerHandler")
    private ChannelHandler webSocketServerHandler;





    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        // TODO Auto-generated method stub
        ch.pipeline().addLast("http-codec", new HttpServerCodec());
        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
        ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
        ch.pipeline().addLast("handler",webSocketServerHandler);
    }

}