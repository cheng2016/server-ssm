package com.cheng.server.android.client;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class SocketClientHandler extends ChannelInboundHandlerAdapter {

    public static final String END_FLAG = new String(new byte[] { 0x01 });

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // TODO Auto-generated method stub
        System.out.println("exceptionCaught：" + cause);
        cause.printStackTrace();
        ctx.close();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("=======================================");

        String message = "C01|1|4a5f9cdc8ec1557f0b8fa2456145439c|868361032348135|5|D01:1|20180711153023|"+END_FLAG;
        ctx.writeAndFlush(message);
        message = "C01|2|4a5f9cdc8ec1557f0b8fa2456145439c|868361032348135|5|D01:1|20180711153500|"+END_FLAG;
        ctx.writeAndFlush(message);
        message = "C02|1|4a5f9cdc8ec1557f0b8fa2456145439c|868361032348135|5|D01:6|20180711153500|\u0001";
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext arg0, Object msg) throws Exception {
        // TODO Auto-generated method stub
        String data = msg.toString();
        System.out.println("client channelRead 数据内容：data：" + "\r\n" + data);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
