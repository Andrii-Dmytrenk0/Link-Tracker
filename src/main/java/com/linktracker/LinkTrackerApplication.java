package com.linktracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point of the Link Tracker application.
 * <p>
 * Async is enabled so click events can be persisted off the request thread,
 * keeping the redirect latency to a minimum. Scheduling is enabled for
 * periodic maintenance jobs (e.g. cleanup of stale rate-limit caches).
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class LinkTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkTrackerApplication.class, args);
    }
}
