package com.cheng.game.network.codec;

import com.cheng.game.common.protocol.MsgIds;
import com.cheng.game.protocol.ChatRequest;
import com.cheng.game.protocol.HeartbeatRequest;
import com.cheng.game.protocol.LoginRequest;
import com.cheng.game.protocol.TokenRefreshRequest;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessageRegistry {

    private final Map<Short, Parser<? extends MessageLite>> parsers = new ConcurrentHashMap<>();

    public MessageRegistry() {
        register(MsgIds.LOGIN_REQ, LoginRequest.parser());
        register(MsgIds.HEARTBEAT_REQ, HeartbeatRequest.parser());
        register(MsgIds.CHAT_REQ, ChatRequest.parser());
        register(MsgIds.TOKEN_REFRESH_REQ, TokenRefreshRequest.parser());
    }

    public void register(short msgId, Parser<? extends MessageLite> parser) {
        parsers.put(msgId, parser);
    }

    public MessageLite parse(short msgId, byte[] payload) throws InvalidProtocolBufferException {
        Parser<? extends MessageLite> parser = parsers.get(msgId);
        if (parser == null) {
            throw new IllegalArgumentException("No protobuf parser for msgId=" + msgId);
        }
        return parser.parseFrom(payload == null ? new byte[0] : payload);
    }

    public boolean supports(short msgId) {
        return parsers.containsKey(msgId);
    }
}
