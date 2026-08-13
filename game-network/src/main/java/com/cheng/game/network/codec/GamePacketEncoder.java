package com.cheng.game.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class GamePacketEncoder extends MessageToByteEncoder<GamePacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, GamePacket msg, ByteBuf out) {
        byte[] payload = msg.payload() == null ? new byte[0] : msg.payload();
        out.writeInt(2 + payload.length);
        out.writeShort(msg.msgId());
        out.writeBytes(payload);
    }
}
