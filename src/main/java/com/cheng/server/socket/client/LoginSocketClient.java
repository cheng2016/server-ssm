package com.cheng.server.socket.client;

import com.cheng.server.entity.User;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoginSocketClient {

    private static final Log logger = LogFactory.getLog(LoginSocketClient.class);
    private static final String IP = "127.0.0.1";
    private static final int PORT = 8088;

    private static final EventLoopGroup group = new NioEventLoopGroup();

    @SuppressWarnings("rawtypes")
    protected static void run() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                // TODO Auto-generated method stub
                ChannelPipeline pipeline = ch.pipeline();
                /*
                 * 这个地方的 必须和服务端对应上。否则无法正常解码和编码
                 *
                 * 解码和编码 我将会在下一张为大家详细的讲解。再次暂时不做详细的描述
                 *
                 * */
                pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                pipeline.addLast("decoder", new StringDecoder());
                pipeline.addLast("encoder", new StringEncoder());
                pipeline.addLast(new SocketClientHandler());
            }
        });

        // 连接服务端
        Channel ch = bootstrap.connect(IP, PORT).sync().channel();


        ch.writeAndFlush("this is a message" + "\r\n");

        logger.info("向Socket服务器发送数据:" + "this is a message" + "\r\n");
    }

    public static void main(String[] args) {
        logger.info("开始连接Socket服务器...");
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
