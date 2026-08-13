package com.cheng.game.network.websocket;

import com.cheng.game.network.codec.GamePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * Bridges WebSocket binary frames and GamePacket using the same length+msgId layout.
 */
public class BinaryWebSocketFrameCodec extends MessageToMessageCodec<BinaryWebSocketFrame, GamePacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, GamePacket msg, List<Object> out) {
        byte[] payload = msg.payload() == null ? new byte[0] : msg.payload();
        ByteBuf buf = ctx.alloc().buffer(4 + 2 + payload.length);
        buf.writeInt(2 + payload.length);
        buf.writeShort(msg.msgId());
        buf.writeBytes(payload);
        out.add(new BinaryWebSocketFrame(buf));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame msg, List<Object> out) {
        ByteBuf in = msg.content();
        if (in.readableBytes() < 6) {
            return;
        }
        int length = in.readInt();
        if (in.readableBytes() < length || length < 2) {
            return;
        }
        short msgId = in.readShort();
        byte[] payload = new byte[length - 2];
        in.readBytes(payload);
        out.add(new GamePacket(msgId, payload));
    }
}
