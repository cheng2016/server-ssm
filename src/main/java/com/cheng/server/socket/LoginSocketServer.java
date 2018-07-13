package com.cheng.server.socket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

@Sharable
@Component
public class LoginSocketServer {
    private static final Log logger = LogFactory.getLog(LoginSocketServer.class);
    private static final String IP = "127.0.0.1";
    private static final int PORT = 8088;

    //分配用于处理业务的线程组数量
    protected static final int BisGroupSize = Runtime.getRuntime().availableProcessors() * 2;
    //每个线程组中线程的数量
    protected static final int worGroupSize = 4;

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(BisGroupSize);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(worGroupSize);

    protected static void run() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                // 以("\n")为结尾分割的 解码器
                pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                pipeline.addLast("decoder", new StringDecoder());
                pipeline.addLast("encoder", new StringEncoder());
                pipeline.addLast(new SocketServerHandler());
            }
        });
        bootstrap.bind(IP, PORT).sync();
        logger.info("Socket服务器已启动完成");
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