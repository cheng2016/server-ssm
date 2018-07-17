package com.cheng.server.android.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * TCP 客户端
 * <p>
 * Created by chengzj 2018/07/13
 */
public class NettySocketClient {
    private final static String TAG = "NettySocketClient";

    public static final String UP_MSG_END_FLAG = new String(new byte[] { 0x01,0x01,0x01 });

    private static final String IP = "127.0.0.1";

//    private static final String IP = "sit.wecarelove.com";

//    private static final int PORT = 2993;

    private static final int PORT = 8080;

    private static final EventLoopGroup group = new NioEventLoopGroup();

    @SuppressWarnings("rawtypes")
    protected static void run() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.handler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                // TODO Auto-generated method stub
                ChannelPipeline pipeline = ch.pipeline();

                ByteBuf delimiter = Unpooled.copiedBuffer(UP_MSG_END_FLAG.getBytes());
                pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, delimiter));
                pipeline.addLast("idleStateHandler",new IdleStateHandler(30 * 60,
                        30 * 60,30 * 60, TimeUnit.SECONDS));
                pipeline.addLast("decoder", new StringDecoder());
                pipeline.addLast("encoder", new StringEncoder());
                pipeline.addLast(new SocketClientHandler());
            }
        });
        // 发起异步连接操作
        ChannelFuture f = bootstrap.connect(IP, PORT).sync();
        // 当代客户端链路关闭
        f.channel().closeFuture().sync();
    }

    public static void main(String[] args) {
        System.out.println("开始连接Socket服务器...");
        try {
            run();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
