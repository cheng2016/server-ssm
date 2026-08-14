package com.cheng.game.network.codec;

import com.cheng.game.common.protocol.MsgIds;
import com.cheng.game.protocol.ChatRequest;
import com.cheng.game.protocol.LoginRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MessageRegistryTest {

    @Test
    void parsesRegisteredProtobufByMsgId() throws Exception {
        MessageRegistry registry = new MessageRegistry();
        LoginRequest login = LoginRequest.newBuilder().setToken("abc").build();
        assertInstanceOf(LoginRequest.class, registry.parse(MsgIds.LOGIN_REQ, login.toByteArray()));
        assertEquals("abc", ((LoginRequest) registry.parse(MsgIds.LOGIN_REQ, login.toByteArray())).getToken());

        ChatRequest chat = ChatRequest.newBuilder().setContent("hi").build();
        assertEquals("hi", ((ChatRequest) registry.parse(MsgIds.CHAT_REQ, chat.toByteArray())).getContent());
    }
}
