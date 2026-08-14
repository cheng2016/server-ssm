package com.cheng.game.app.game;

import com.cheng.game.app.config.GameProtectProperties;
import org.springframework.stereotype.Component;

@Component
public class ChatGuard {

    private final GameProtectProperties properties;

    public ChatGuard(GameProtectProperties properties) {
        this.properties = properties;
    }

    public String rejectReason(String content) {
        if (content == null || content.isBlank()) {
            return "empty chat";
        }
        if (content.length() > properties.getChatMaxLength()) {
            return "chat too long";
        }
        String normalized = content.toLowerCase();
        for (String word : properties.getSensitiveWords()) {
            if (word != null && !word.isBlank() && normalized.contains(word.toLowerCase())) {
                return "sensitive word";
            }
        }
        return null;
    }
}
