package com.cheng.game.network.session;

import io.netty.channel.Channel;

import java.time.Instant;

public class PlayerSession {

    private final Long playerId;
    private final String nickname;
    private final Channel channel;
    private volatile Instant lastActiveAt;

    public PlayerSession(Long playerId, String nickname, Channel channel) {
        this.playerId = playerId;
        this.nickname = nickname;
        this.channel = channel;
        this.lastActiveAt = Instant.now();
    }

    public Long getPlayerId() {
        return playerId;
    }

    public String getNickname() {
        return nickname;
    }

    public Channel getChannel() {
        return channel;
    }

    public Instant getLastActiveAt() {
        return lastActiveAt;
    }

    public void touch() {
        this.lastActiveAt = Instant.now();
    }
}
