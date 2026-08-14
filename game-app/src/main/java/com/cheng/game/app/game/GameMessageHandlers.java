package com.cheng.game.app.game;

import com.cheng.game.app.security.JwtService;
import com.cheng.game.app.service.UserAuthService;
import com.cheng.game.common.error.ErrorCode;
import com.cheng.game.common.protocol.MsgIds;
import com.cheng.game.network.codec.GamePacket;
import com.cheng.game.network.codec.SystemNotifies;
import com.cheng.game.network.handler.GameHandler;
import com.cheng.game.network.handler.GameMessageController;
import com.cheng.game.network.session.SessionManager;
import com.cheng.game.protocol.ChatMessage;
import com.cheng.game.protocol.ChatRequest;
import com.cheng.game.protocol.HeartbeatRequest;
import com.cheng.game.protocol.HeartbeatResponse;
import com.cheng.game.protocol.LoginRequest;
import com.cheng.game.protocol.LoginResponse;
import com.cheng.game.protocol.NotifyReason;
import com.cheng.game.protocol.TokenRefreshRequest;
import com.cheng.game.protocol.TokenRefreshResponse;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GameMessageController
public class GameMessageHandlers {

    private static final Logger log = LoggerFactory.getLogger(GameMessageHandlers.class);

    private final JwtService jwtService;
    private final SessionManager sessionManager;
    private final UserAuthService userAuthService;
    private final ChatGuard chatGuard;

    public GameMessageHandlers(JwtService jwtService,
                               SessionManager sessionManager,
                               UserAuthService userAuthService,
                               ChatGuard chatGuard) {
        this.jwtService = jwtService;
        this.sessionManager = sessionManager;
        this.userAuthService = userAuthService;
        this.chatGuard = chatGuard;
    }

    @GameHandler(msgId = MsgIds.LOGIN_REQ)
    public GamePacket login(ChannelHandlerContext ctx, LoginRequest request) {
        try {
            JwtService.TokenPayload token = jwtService.parse(request.getToken());
            sessionManager.bind(token.userId(), token.nickname(), ctx.channel());
            userAuthService.markOnline(token.userId());
            LoginResponse response = LoginResponse.newBuilder()
                    .setCode(ErrorCode.OK.code())
                    .setMessage("login ok")
                    .setPlayerId(token.userId())
                    .setNickname(token.nickname())
                    .build();
            log.info("Player login playerId={} nick={}", token.userId(), token.nickname());
            return new GamePacket(MsgIds.LOGIN_RESP, response.toByteArray());
        } catch (Exception e) {
            LoginResponse response = LoginResponse.newBuilder()
                    .setCode(ErrorCode.INVALID_TOKEN.code())
                    .setMessage("invalid token")
                    .build();
            return new GamePacket(MsgIds.LOGIN_RESP, response.toByteArray());
        }
    }

    @GameHandler(msgId = MsgIds.HEARTBEAT_REQ)
    public GamePacket heartbeat(ChannelHandlerContext ctx, HeartbeatRequest request) {
        sessionManager.get(ctx.channel().attr(SessionManager.PLAYER_ID).get())
                .ifPresent(session -> session.touch());
        HeartbeatResponse response = HeartbeatResponse.newBuilder()
                .setServerTime(System.currentTimeMillis())
                .build();
        return new GamePacket(MsgIds.HEARTBEAT_RESP, response.toByteArray());
    }

    @GameHandler(msgId = MsgIds.CHAT_REQ)
    public GamePacket chat(ChannelHandlerContext ctx, ChatRequest request) {
        Long playerId = ctx.channel().attr(SessionManager.PLAYER_ID).get();
        String nickname = ctx.channel().attr(SessionManager.NICKNAME).get();
        if (playerId == null) {
            return null;
        }
        String reject = chatGuard.rejectReason(request.getContent());
        if (reject != null) {
            return SystemNotifies.of(ErrorCode.BAD_CONTENT, NotifyReason.BAD_CONTENT);
        }
        ChatMessage message = ChatMessage.newBuilder()
                .setPlayerId(playerId)
                .setNickname(nickname == null ? "unknown" : nickname)
                .setContent(request.getContent())
                .setTimestamp(System.currentTimeMillis())
                .build();
        sessionManager.broadcast(new GamePacket(MsgIds.CHAT_MSG, message.toByteArray()));
        return null;
    }

    @GameHandler(msgId = MsgIds.TOKEN_REFRESH_REQ)
    public GamePacket refreshToken(ChannelHandlerContext ctx, TokenRefreshRequest request) {
        Long playerId = ctx.channel().attr(SessionManager.PLAYER_ID).get();
        if (playerId == null) {
            return new GamePacket(MsgIds.TOKEN_REFRESH_RESP, TokenRefreshResponse.newBuilder()
                    .setCode(ErrorCode.UNAUTHORIZED.code())
                    .setMessage("login required")
                    .build()
                    .toByteArray());
        }
        try {
            String token = userAuthService.refreshToken(playerId);
            return new GamePacket(MsgIds.TOKEN_REFRESH_RESP, TokenRefreshResponse.newBuilder()
                    .setCode(ErrorCode.OK.code())
                    .setMessage("ok")
                    .setToken(token)
                    .build()
                    .toByteArray());
        } catch (Exception e) {
            return new GamePacket(MsgIds.TOKEN_REFRESH_RESP, TokenRefreshResponse.newBuilder()
                    .setCode(ErrorCode.INVALID_TOKEN.code())
                    .setMessage("refresh failed")
                    .build()
                    .toByteArray());
        }
    }
}
