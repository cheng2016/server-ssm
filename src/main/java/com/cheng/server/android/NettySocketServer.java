package com.cheng.server.android;


import com.cheng.server.controller.UserController;
import com.cheng.server.entity.AddressBookProtos;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

/**
 * TCP 服务器
 * <p>
 * Created by chengzj 2018/07/13
 */
@Sharable
public class NettySocketServer {
    private static final Log logger = LogFactory.getLog(UserController.class);
    private static final String IP = "127.0.0.1";
    private static final int PORT = 8080;

    //分配用于处理业务的线程组数量
    protected static final int BisGroupSize = Runtime.getRuntime().availableProcessors() * 2;
    //每个线程组中线程的数量
    protected static final int worGroupSize = 4;

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(BisGroupSize);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(worGroupSize);

    public static final String UP_MSG_END_FLAG = new String(new byte[]{0x01});

    protected static void run() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.handler(new LoggingHandler(LogLevel.INFO));
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                ByteBuf delimiter = Unpooled.copiedBuffer(UP_MSG_END_FLAG.getBytes());
                pipeline.addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, delimiter));
                pipeline.addLast("idleStateHandler",new IdleStateHandler(30 * 60,
                        30 * 60,30 * 60, TimeUnit.SECONDS));
                pipeline.addLast("decoder", new StringDecoder());
                pipeline.addLast("encoder", new StringEncoder());

                pipeline.addLast(new SocketServerHandler());
            }
        });
        bootstrap.bind(IP, PORT).sync();
        logger.info("Socket服务器已启动完成，端口号为：" + PORT);
    }

    protected static void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        logger.info("开始启动Socket服务器...");
        run();
    }
}