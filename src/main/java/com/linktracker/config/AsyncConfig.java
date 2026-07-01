package com.linktracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Dedicated thread pool for click-tracking enrichment (GeoIP + UA parsing +
 * persistence), kept separate from the web server's request-handling
 * threads so that high redirect throughput never starves background
 * tracking work, and vice versa.
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "clickTrackingExecutor")
    public Executor clickTrackingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(5000);
        executor.setThreadNamePrefix("click-track-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
