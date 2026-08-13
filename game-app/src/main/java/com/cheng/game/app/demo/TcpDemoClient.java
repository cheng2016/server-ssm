package com.cheng.game.app.demo;

import com.cheng.game.common.protocol.MsgIds;
import com.cheng.game.protocol.ChatMessage;
import com.cheng.game.protocol.ChatRequest;
import com.cheng.game.protocol.HeartbeatRequest;
import com.cheng.game.protocol.HeartbeatResponse;
import com.cheng.game.protocol.LoginRequest;
import com.cheng.game.protocol.LoginResponse;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Tiny TCP client for manual smoke tests.
 * Usage: java -cp ... com.cheng.game.app.demo.TcpDemoClient &lt;host&gt; &lt;port&gt; &lt;jwt&gt;
 */
public final class TcpDemoClient {

    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 9000;
        String token = args.length > 2 ? args[2] : "";
        if (token.isBlank()) {
            System.err.println("Usage: TcpDemoClient <host> <port> <jwt>");
            System.exit(1);
        }

        try (Socket socket = new Socket(host, port)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            writePacket(out, MsgIds.LOGIN_REQ, LoginRequest.newBuilder().setToken(token).build().toByteArray());
            Packet login = readPacket(in);
            LoginResponse loginResponse = LoginResponse.parseFrom(login.payload);
            System.out.println("LOGIN code=" + loginResponse.getCode() + " nick=" + loginResponse.getNickname());

            writePacket(out, MsgIds.HEARTBEAT_REQ,
                    HeartbeatRequest.newBuilder().setClientTime(System.currentTimeMillis()).build().toByteArray());
            Packet hb = readPacket(in);
            HeartbeatResponse heartbeatResponse = HeartbeatResponse.parseFrom(hb.payload);
            System.out.println("HEARTBEAT serverTime=" + heartbeatResponse.getServerTime());

            writePacket(out, MsgIds.CHAT_REQ,
                    ChatRequest.newBuilder().setContent("hello from TcpDemoClient").build().toByteArray());
            Packet chat = readPacket(in);
            ChatMessage chatMessage = ChatMessage.parseFrom(chat.payload);
            System.out.println("CHAT from=" + chatMessage.getNickname()
                    + " content=" + chatMessage.getContent()
                    + " utf8=" + new String(chatMessage.getContent().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }
    }

    private static void writePacket(DataOutputStream out, short msgId, byte[] payload) throws Exception {
        out.writeInt(2 + payload.length);
        out.writeShort(msgId);
        out.write(payload);
        out.flush();
    }

    private static Packet readPacket(DataInputStream in) throws Exception {
        int length = in.readInt();
        short msgId = in.readShort();
        byte[] payload = in.readNBytes(length - 2);
        return new Packet(msgId, payload);
    }

    private record Packet(short msgId, byte[] payload) {
    }
}
