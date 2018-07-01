package com.cheng.server.socket.client;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketClientHandler extends SimpleChannelInboundHandler<String> {
    private static final Log logger = LogFactory.getLog(LoginSocketClient.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext arg0, Throwable arg1) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void channelRead(ChannelHandlerContext arg0, Object msg) throws Exception {
        // TODO Auto-generated method stub
        String data = msg.toString();
        logger.info("channelRead 数据内容：data="+data);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext arg0, String data) throws Exception {
        // TODO Auto-generated method stub
        logger.info("channelRead0 数据内容：data="+data);
    }
}
