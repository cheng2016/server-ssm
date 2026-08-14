package com.cheng.game.app.game;

import com.cheng.game.app.config.GameProtectProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChatGuardTest {

    @Test
    void rejectsEmptyTooLongAndSensitiveWords() {
        GameProtectProperties properties = new GameProtectProperties();
        properties.setChatMaxLength(8);
        properties.setSensitiveWords(List.of("赌博"));
        ChatGuard guard = new ChatGuard(properties);

        assertEquals("empty chat", guard.rejectReason("  "));
        assertEquals("chat too long", guard.rejectReason("123456789"));
        assertEquals("sensitive word", guard.rejectReason("涉嫌赌博"));
        assertNull(guard.rejectReason("hello"));
        assertNotNull(guard.rejectReason(null));
    }
}
