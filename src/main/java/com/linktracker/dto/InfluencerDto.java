package com.linktracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Read model for an influencer, including the full tracking link and a
 * running click count, returned by the REST API and used in the admin UI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfluencerDto {
    private Long id;
    private String name;
    private String code;
    private String instagramUrl;
    private String trackingUrl;
    private boolean active;
    private long totalClicks;
    private Instant createdAt;
}
