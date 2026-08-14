package com.cheng.game.network.limit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WindowRateLimiterTest {

    @Test
    void rejectsWhenWindowIsFull() {
        WindowRateLimiter limiter = new WindowRateLimiter();
        assertTrue(limiter.tryAcquire("chat:1", 2, 1000));
        assertTrue(limiter.tryAcquire("chat:1", 2, 1000));
        assertFalse(limiter.tryAcquire("chat:1", 2, 1000));
    }

    @Test
    void isolatesKeys() {
        WindowRateLimiter limiter = new WindowRateLimiter();
        assertTrue(limiter.tryAcquire("a", 1, 1000));
        assertTrue(limiter.tryAcquire("b", 1, 1000));
        assertFalse(limiter.tryAcquire("a", 1, 1000));
    }
}
