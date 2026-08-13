package com.cheng.game.network.codec;

public record GamePacket(short msgId, byte[] payload) {
}
