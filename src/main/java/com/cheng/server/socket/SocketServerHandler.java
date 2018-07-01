package com.cheng.server.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketServerHandler extends SimpleChannelInboundHandler<String> {
    private static final Log logger = LogFactory.getLog(LoginSocketServer.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext arg0, Throwable arg1) throws Exception {
        // TODO Auto-generated method stub
        logger.info("exceptionCaught");
    }

    @Override
    public void channelRead(ChannelHandlerContext arg0, Object msg) throws Exception {
        // TODO Auto-generated method stub
        logger.info("channelRead 数据内容：data="+msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        logger.info("channelRead0");
    }

}
