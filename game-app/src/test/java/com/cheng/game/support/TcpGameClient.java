package com.cheng.game.support;

import com.cheng.game.common.protocol.MsgIds;
import com.cheng.game.protocol.ChatMessage;
import com.cheng.game.protocol.ChatRequest;
import com.cheng.game.protocol.HeartbeatRequest;
import com.cheng.game.protocol.HeartbeatResponse;
import com.cheng.game.protocol.LoginRequest;
import com.cheng.game.protocol.LoginResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;

public final class TcpGameClient implements AutoCloseable {

    private final Socket socket;
    private final DataOutputStream out;
    private final DataInputStream in;

    public TcpGameClient(String host, int port) throws IOException {
        this.socket = new Socket();
        this.socket.connect(new InetSocketAddress(host, port), (int) Duration.ofSeconds(5).toMillis());
        this.socket.setSoTimeout((int) Duration.ofSeconds(5).toMillis());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
    }

    public LoginResponse login(String token) throws Exception {
        write(MsgIds.LOGIN_REQ, LoginRequest.newBuilder().setToken(token).build().toByteArray());
        Packet packet = read();
        if (packet.msgId != MsgIds.LOGIN_RESP) {
            throw new IllegalStateException("Expected LOGIN_RESP, got " + packet.msgId);
        }
        return LoginResponse.parseFrom(packet.payload);
    }

    public HeartbeatResponse heartbeat() throws Exception {
        write(MsgIds.HEARTBEAT_REQ,
                HeartbeatRequest.newBuilder().setClientTime(System.currentTimeMillis()).build().toByteArray());
        Packet packet = read();
        if (packet.msgId != MsgIds.HEARTBEAT_RESP) {
            throw new IllegalStateException("Expected HEARTBEAT_RESP, got " + packet.msgId);
        }
        return HeartbeatResponse.parseFrom(packet.payload);
    }

    public void sendChat(String content) throws Exception {
        write(MsgIds.CHAT_REQ, ChatRequest.newBuilder().setContent(content).build().toByteArray());
    }

    public ChatMessage readChat() throws Exception {
        Packet packet = read();
        if (packet.msgId != MsgIds.CHAT_MSG) {
            throw new IllegalStateException("Expected CHAT_MSG, got " + packet.msgId);
        }
        return ChatMessage.parseFrom(packet.payload);
    }

    private void write(short msgId, byte[] payload) throws IOException {
        out.writeInt(2 + payload.length);
        out.writeShort(msgId);
        out.write(payload);
        out.flush();
    }

    private Packet read() throws IOException {
        int length = in.readInt();
        short msgId = in.readShort();
        byte[] payload = in.readNBytes(length - 2);
        return new Packet(msgId, payload);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    private record Packet(short msgId, byte[] payload) {
    }
}
