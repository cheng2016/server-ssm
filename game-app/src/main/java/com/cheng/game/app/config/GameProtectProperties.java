package com.cheng.game.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "game.protect")
public class GameProtectProperties {

    private int chatMaxLength = 128;
    private List<String> sensitiveWords = new ArrayList<>(List.of("赌博", "诈骗"));

    public int getChatMaxLength() {
        return chatMaxLength;
    }

    public void setChatMaxLength(int chatMaxLength) {
        this.chatMaxLength = chatMaxLength;
    }

    public List<String> getSensitiveWords() {
        return sensitiveWords;
    }

    public void setSensitiveWords(List<String> sensitiveWords) {
        this.sensitiveWords = sensitiveWords;
    }
}
