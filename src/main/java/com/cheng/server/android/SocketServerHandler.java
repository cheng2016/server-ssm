package com.cheng.server.android;


import com.cheng.server.controller.UserController;
import com.cheng.server.entity.AddressBookProtos;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketServerHandler extends ChannelInboundHandlerAdapter {
    public static final String TAG = "SocketServerHandler";
    public static final String END_FLAG = new String(new byte[]{0x01, 0x01, 0x01});
    private static final Log logger = LogFactory.getLog(UserController.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("channelActive，ChatClient:" + incoming.remoteAddress() + "上线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
        logger.info("ChatClient:" + incoming.remoteAddress() + "掉线");
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        // 当出现异常就关闭连接
        logger.info("ChatClient:" + incoming.remoteAddress()
                + "异常,已被服务器关闭");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = (String) msg;
        logger.info("server channelRead 数据内容：data：" + "\r\n" + message);
//        System.out.println("channelRead 数据内容：data："+ msg);
        ctx.writeAndFlush(message + END_FLAG);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                logger.info("userEventTriggered READER_IDLE 读超时，长期没收到服务器推送数据！");
            } else if (event.state().equals(IdleState.WRITER_IDLE)) {
                logger.info("userEventTriggered WRITER_IDLE 写超时，长期未向服务器发送数据！");
            } else if (event.state().equals(IdleState.ALL_IDLE)) {
                logger.info("userEventTriggered ALL 没有接收或发送数据一段时间");
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
