package com.cheng.server.protobuf.server;


import com.cheng.server.entity.AddressBookProtos;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * TCP 服务器
 * <p>
 * Created by chengzj 2018/07/13
 */
@Sharable
public class LoginSocketServer {
    private static final String IP = "127.0.0.1";
    private static final int PORT = 2993;

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
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.handler(new LoggingHandler(LogLevel.INFO));
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                // Protobuf半包处理器
                pipeline.addLast(new ProtobufVarint32FrameDecoder());
                // 配置Protobuf解码处理器
                pipeline.addLast(new ProtobufDecoder(AddressBookProtos.Person.getDefaultInstance()));
                // 用于在序列化的字节数组前加上一个简单的包头，只包含序列化的字节长度。
                pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                //配置Protobuf编码器，发送的消息会先经过编码
                pipeline.addLast(new ProtobufEncoder());
                pipeline.addLast(new SocketServerHandler());
            }
        });
        bootstrap.bind(IP, PORT).sync();
        System.out.println("Socket服务器已启动完成，端口号为：" + PORT);
    }

    protected static void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("开始启动Socket服务器...");
        run();
    }
}