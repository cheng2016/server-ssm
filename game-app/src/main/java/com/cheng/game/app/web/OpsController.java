package com.cheng.game.app.web;

import com.cheng.game.app.service.UserAuthService;
import com.cheng.game.common.api.ApiResponse;
import com.cheng.game.common.error.BusinessException;
import com.cheng.game.common.error.ErrorCode;
import com.cheng.game.common.protocol.MsgIds;
import com.cheng.game.network.codec.GamePacket;
import com.cheng.game.network.session.SessionManager;
import com.cheng.game.protocol.ChatMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ops")
@Tag(name = "Ops")
public class OpsController {

    private final SessionManager sessionManager;
    private final UserAuthService userAuthService;

    public OpsController(SessionManager sessionManager, UserAuthService userAuthService) {
        this.sessionManager = sessionManager;
        this.userAuthService = userAuthService;
    }

    @GetMapping("/online")
    @Operation(summary = "Online player count and list")
    public ApiResponse<OnlineView> online() {
        List<OnlinePlayer> players = sessionManager.allSessions().stream()
                .map(s -> new OnlinePlayer(s.getPlayerId(), s.getNickname()))
                .toList();
        return ApiResponse.ok(new OnlineView(sessionManager.onlineCount(), userAuthService.redisOnlineCount(), players));
    }

    @PostMapping("/kick/{playerId}")
    @Operation(summary = "Kick an online player")
    public ApiResponse<Void> kick(@PathVariable Long playerId) {
        boolean kicked = sessionManager.kick(playerId);
        if (!kicked) {
            throw new BusinessException(ErrorCode.PLAYER_OFFLINE);
        }
        userAuthService.markOffline(playerId);
        return ApiResponse.ok();
    }

    @PostMapping("/broadcast")
    @Operation(summary = "Broadcast a system chat message to all online players")
    public ApiResponse<Void> broadcast(@Valid @RequestBody BroadcastRequest request) {
        ChatMessage message = ChatMessage.newBuilder()
                .setPlayerId(0)
                .setNickname("system")
                .setContent(request.content())
                .setTimestamp(System.currentTimeMillis())
                .build();
        sessionManager.broadcast(new GamePacket(MsgIds.CHAT_MSG, message.toByteArray()));
        return ApiResponse.ok();
    }

    public record OnlinePlayer(Long playerId, String nickname) {
    }

    public record OnlineView(int sessionCount, long redisCount, List<OnlinePlayer> players) {
    }

    public record BroadcastRequest(@NotBlank String content) {
    }
}
