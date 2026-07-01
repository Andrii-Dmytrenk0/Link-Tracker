package com.linktracker.controller;

import com.linktracker.entity.Influencer;
import com.linktracker.service.ClickTrackingService;
import com.linktracker.service.InfluencerService;
import com.linktracker.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Public-facing redirect endpoint. This is the hottest path in the system:
 * it must resolve the influencer, kick off async tracking, and respond with
 * a redirect as fast as possible, with no synchronous DB writes or external
 * HTTP calls on the request thread.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class RedirectController {

    /** Fallback destination used if an unknown/inactive code is requested. */
    private static final String FALLBACK_URL = "https://instagram.com";

    private final InfluencerService influencerService;
    private final ClickTrackingService clickTrackingService;

    @GetMapping("/i/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest request) {
        Optional<Influencer> influencerOpt = influencerService.findActiveByCode(code);

        if (influencerOpt.isEmpty()) {
            log.debug("Redirect requested for unknown or inactive code '{}'", code);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, FALLBACK_URL)
                    .build();
        }

        Influencer influencer = influencerOpt.get();
        String ip = ClientIpResolver.resolve(request);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String referer = request.getHeader(HttpHeaders.REFERER);

        // Fire-and-forget: enrichment + persistence happen on a background
        // thread pool and never block this response.
        clickTrackingService.trackClickAsync(influencer, ip, userAgent, referer);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, influencer.getInstagramUrl())
                .build();
    }
}
