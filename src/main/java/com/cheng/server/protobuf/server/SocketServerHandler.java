package com.cheng.server.protobuf.server;


import com.cheng.server.entity.AddressBookProtos;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class SocketServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("channelActive，ChatClient:" + incoming.remoteAddress() + "上线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
        System.out.println("ChatClient:" + incoming.remoteAddress() + "掉线");
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        // 当出现异常就关闭连接
        System.out.println("ChatClient:" + incoming.remoteAddress()
                + "异常,已被服务器关闭");
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        AddressBookProtos.Person p = (AddressBookProtos.Person) msg;
        System.out.println("server channelRead 数据内容：data：" + "\r\n" + p);
//        System.out.println("channelRead 数据内容：data："+ msg);
        ctx.writeAndFlush(p);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
