package com.cheng.server.protobuf;

import com.cheng.server.entity.AddressBookProtos;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

/**
 * TCP 客户端
 * <p>
 * Created by chengzj 2018/07/13
 */
public class LoginSocketClient {
    private final static String TAG = "LoginSocketClient";
    private static final String IP = "127.0.0.1";

    private static final int PORT = 2993;

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
                // Protobuf半包处理器
                pipeline.addLast(new ProtobufVarint32FrameDecoder());
                // 配置Protobuf解码处理器
                pipeline.addLast(new ProtobufDecoder(AddressBookProtos.Person.getDefaultInstance()));
                // 用于在序列化的字节数组前加上一个简单的包头，只包含序列化的字节长度。
                pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                //配置Protobuf编码器，发送的消息会先经过编码
                pipeline.addLast(new ProtobufEncoder());
                pipeline.addLast(new SocketClientHandler());
            }
        });
        // 发起异步连接操作
        ChannelFuture f = bootstrap.connect(IP, PORT).sync();
        // 当代客户端链路关闭
        f.channel().closeFuture().sync();

        // 连接服务端
/*        Channel ch = bootstrap.connect(IP, PORT).sync().channel();
        AddressBookProtos.Person person = AddressBookProtos.Person.newBuilder()
                .setId(1)
                .setName("text")
                .setEmail("123")
                .build();
        ch.writeAndFlush(person);
        ch.closeFuture().sync();*/
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
