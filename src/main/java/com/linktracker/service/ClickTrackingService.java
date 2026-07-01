package com.linktracker.service;

import com.linktracker.entity.Influencer;

/**
 * Records click events. The redirect-critical path only needs to capture the
 * raw request data (IP, UA, referer); enrichment (geolocation, UA parsing,
 * bot scoring) happens asynchronously so it never delays the redirect.
 */
public interface ClickTrackingService {

    /**
     * Captures raw request data synchronously (cheap) and schedules
     * asynchronous enrichment + persistence. Must return almost instantly.
     */
    void trackClickAsync(Influencer influencer, String ip, String userAgent, String referer);
}
