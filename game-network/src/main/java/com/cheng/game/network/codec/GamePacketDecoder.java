package com.cheng.game.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Frame: int32 length (payload bytes after length field) + int16 msgId + body.
 * Length = 2 + body.length
 */
public class GamePacketDecoder extends ByteToMessageDecoder {

    private static final int MAX_FRAME = 1024 * 1024;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int length = in.readInt();
        if (length <= 0 || length > MAX_FRAME) {
            ctx.close();
            return;
        }
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        if (length < 2) {
            ctx.close();
            return;
        }
        short msgId = in.readShort();
        byte[] payload = new byte[length - 2];
        in.readBytes(payload);
        out.add(new GamePacket(msgId, payload));
    }
}
