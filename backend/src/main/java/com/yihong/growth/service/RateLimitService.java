package com.yihong.growth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易内存限流。单实例有效，重启清空。多实例需 Redis。
 * 仅用 request.getRemoteAddr()，不信任 X-Forwarded-For。
 */
@Slf4j
@Service
public class RateLimitService {

    private final Map<String, Long> lastSubmit = new ConcurrentHashMap<>();
    private static final long INTERVAL_MS = 60_000;
    private static final int MAX_ENTRIES = 5_000;

    public boolean tryAcquire(String ip) {
        long now = System.currentTimeMillis();
        long cutoff = now - INTERVAL_MS;

        Long existing = lastSubmit.get(ip);
        if (existing != null && existing > cutoff) {
            log.debug("Rate limit hit: ip={}", ip);
            return false;
        }
        lastSubmit.put(ip, now);

        if (lastSubmit.size() > MAX_ENTRIES) {
            lastSubmit.entrySet().removeIf(e -> e.getValue() < cutoff);
        }
        return true;
    }
}
