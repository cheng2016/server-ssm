package com.cheng.game.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GamePacketCodecTest {

    @Test
    void encodeAndDecodeRoundTrip() {
        EmbeddedChannel encodeChannel = new EmbeddedChannel(new GamePacketEncoder());
        byte[] body = new byte[]{1, 2, 3, 4};
        assertTrue(encodeChannel.writeOutbound(new GamePacket((short) 1001, body)));
        ByteBuf encoded = encodeChannel.readOutbound();

        EmbeddedChannel decodeChannel = new EmbeddedChannel(new GamePacketDecoder());
        assertTrue(decodeChannel.writeInbound(encoded));
        GamePacket decoded = decodeChannel.readInbound();
        assertEquals(1001, decoded.msgId());
        assertArrayEquals(body, decoded.payload());
    }

    @Test
    void waitsForFullFrame() {
        EmbeddedChannel channel = new EmbeddedChannel(new GamePacketDecoder());
        ByteBuf partial = Unpooled.buffer();
        partial.writeInt(6);
        partial.writeShort(1003);
        // length=6 means msgId(2) + body(4); only msgId is present, so wait
        channel.writeInbound(partial);
        assertNull(channel.readInbound());
    }
}
