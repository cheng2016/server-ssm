package com.cheng.game.network.limit;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WindowRateLimiter {

    private final Map<String, ArrayDeque<Long>> windows = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key, int limit, long windowMs) {
        if (limit <= 0) {
            return true;
        }
        long now = System.currentTimeMillis();
        long cutoff = now - windowMs;
        ArrayDeque<Long> hits = windows.computeIfAbsent(key, ignored -> new ArrayDeque<>());
        synchronized (hits) {
            evict(hits, cutoff);
            if (hits.size() >= limit) {
                return false;
            }
            hits.addLast(now);
            return true;
        }
    }

    public void evictExpired(long maxIdleMs) {
        long cutoff = System.currentTimeMillis() - maxIdleMs;
        for (Map.Entry<String, ArrayDeque<Long>> entry : windows.entrySet()) {
            ArrayDeque<Long> hits = entry.getValue();
            synchronized (hits) {
                evict(hits, cutoff);
                if (hits.isEmpty()) {
                    windows.remove(entry.getKey(), hits);
                }
            }
        }
    }

    private static void evict(ArrayDeque<Long> hits, long cutoff) {
        Iterator<Long> iterator = hits.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() >= cutoff) {
                break;
            }
            iterator.remove();
        }
    }
}
