package com.linktracker.service.impl;

import com.linktracker.entity.Influencer;
import com.linktracker.service.ClickTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Default {@link ClickTrackingService} implementation. The actual enrichment
 * work is delegated to {@link ClickEnrichmentService}, a separate Spring
 * bean, so that its {@code @Async} annotation is honored by Spring's proxy
 * (self-invocation within the same class would otherwise bypass the proxy
 * and run synchronously, delaying the redirect).
 */
@Service
@RequiredArgsConstructor
public class ClickTrackingServiceImpl implements ClickTrackingService {

    private final ClickEnrichmentService clickEnrichmentService;

    @Override
    public void trackClickAsync(Influencer influencer, String ip, String userAgent, String referer) {
        // Pass only the id across the async boundary to avoid sharing a
        // detached entity between threads/transactions.
        clickEnrichmentService.enrichAndPersist(influencer.getId(), ip, userAgent, referer);
    }
}
