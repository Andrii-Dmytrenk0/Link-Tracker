package com.linktracker.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight in-memory rate limiter used to flag suspicious repeat clicks
 * (the same influencer + IP combination clicking many times in a short
 * window). This complements, but does not replace, the User-Agent based bot
 * detection in {@link UserAgentParsingService}.
 */
@Service
public class BotDetectionService {

    private final Cache<String, AtomicInteger> recentClicks;
    private final int suspiciousThreshold;

    public BotDetectionService(
            @Value("${tracking.rate-limit.window-seconds:60}") long windowSeconds,
            @Value("${tracking.rate-limit.suspicious-threshold:5}") int suspiciousThreshold) {
        this.suspiciousThreshold = suspiciousThreshold;
        this.recentClicks = Caffeine.newBuilder()
                .expireAfterWrite(windowSeconds, TimeUnit.SECONDS)
                .maximumSize(100_000)
                .build();
    }

    /**
     * Registers a click from the given influencer/IP pair and returns whether
     * it should be flagged as suspicious (too many clicks in the rate-limit
     * window).
     */
    public boolean registerAndCheckSuspicious(Long influencerId, String ip) {
        if (ip == null) {
            return false;
        }
        String key = influencerId + ":" + ip;
        AtomicInteger counter = recentClicks.get(key, k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();
        return count > suspiciousThreshold;
    }
}
