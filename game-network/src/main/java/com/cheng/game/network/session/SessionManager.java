package com.cheng.game.network.session;

import com.cheng.game.common.error.ErrorCode;
import com.cheng.game.network.codec.GamePacket;
import com.cheng.game.network.codec.SystemNotifies;
import com.cheng.game.protocol.NotifyReason;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SessionManager {

    public static final AttributeKey<Long> PLAYER_ID = AttributeKey.valueOf("playerId");
    public static final AttributeKey<String> NICKNAME = AttributeKey.valueOf("nickname");

    private final Map<Long, PlayerSession> playerSessions = new ConcurrentHashMap<>();
    private final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final ChannelGroup connectedChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final AtomicLong kickCount = new AtomicLong();

    public void track(Channel channel) {
        connectedChannels.add(channel);
    }

    public void bind(Long playerId, String nickname, Channel channel) {
        Channel previous = Optional.ofNullable(playerSessions.get(playerId))
                .map(PlayerSession::getChannel)
                .orElse(null);
        if (previous != null && previous != channel && previous.isActive()) {
            previous.writeAndFlush(SystemNotifies.of(ErrorCode.REPLACED, NotifyReason.REPLACED))
                    .addListener(ChannelFutureListener.CLOSE);
        }
        channel.attr(PLAYER_ID).set(playerId);
        channel.attr(NICKNAME).set(nickname);
        playerSessions.put(playerId, new PlayerSession(playerId, nickname, channel));
        allChannels.add(channel);
    }

    public void unbind(Channel channel) {
        Long playerId = channel.attr(PLAYER_ID).get();
        if (playerId != null) {
            PlayerSession session = playerSessions.get(playerId);
            if (session != null && session.getChannel() == channel) {
                playerSessions.remove(playerId);
            }
        }
        allChannels.remove(channel);
        connectedChannels.remove(channel);
    }

    public Optional<PlayerSession> get(Long playerId) {
        return Optional.ofNullable(playerSessions.get(playerId));
    }

    public Collection<PlayerSession> allSessions() {
        return playerSessions.values();
    }

    public int onlineCount() {
        return playerSessions.size();
    }

    public int connectionCount() {
        return connectedChannels.size();
    }

    public long kickCount() {
        return kickCount.get();
    }

    public boolean kick(Long playerId) {
        PlayerSession session = playerSessions.remove(playerId);
        if (session == null) {
            return false;
        }
        kickCount.incrementAndGet();
        Channel channel = session.getChannel();
        if (channel.isActive()) {
            channel.writeAndFlush(SystemNotifies.of(ErrorCode.KICKED, NotifyReason.KICKED))
                    .addListener(ChannelFutureListener.CLOSE);
        } else {
            channel.close();
        }
        return true;
    }

    public void send(Long playerId, GamePacket packet) {
        PlayerSession session = playerSessions.get(playerId);
        if (session != null && session.getChannel().isActive()) {
            session.getChannel().writeAndFlush(packet);
        }
    }

    public void broadcast(GamePacket packet) {
        allChannels.writeAndFlush(packet);
    }

    public ChannelGroup allChannels() {
        return allChannels;
    }
}
