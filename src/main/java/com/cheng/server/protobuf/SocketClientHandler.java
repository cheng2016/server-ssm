package com.cheng.server.protobuf;


import com.cheng.server.entity.AddressBookProtos;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class SocketClientHandler extends ChannelInboundHandlerAdapter {

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

        AddressBookProtos.Person.PhoneNumber phoneNumber = AddressBookProtos.Person.PhoneNumber.newBuilder()
                .setNumber("18202745852")
                .setType(AddressBookProtos.Person.PhoneType.HOME)
                .build();
        AddressBookProtos.Person person = AddressBookProtos.Person.newBuilder()
                .setId(1)
                .setName("text")
                .setEmail("123")
                .addPhone(phoneNumber)
                .build();
        ctx.writeAndFlush(person);
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
