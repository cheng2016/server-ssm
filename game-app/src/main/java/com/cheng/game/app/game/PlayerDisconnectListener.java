package com.cheng.game.app.game;

import com.cheng.game.app.service.UserAuthService;
import com.cheng.game.network.session.PlayerDisconnectedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PlayerDisconnectListener {

    private final UserAuthService userAuthService;

    public PlayerDisconnectListener(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @EventListener
    public void onDisconnect(PlayerDisconnectedEvent event) {
        userAuthService.markOffline(event.playerId());
    }
}
