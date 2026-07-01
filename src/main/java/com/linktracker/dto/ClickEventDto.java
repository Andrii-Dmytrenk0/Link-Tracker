package com.linktracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Read model for a single click event, used for the "last 100 clicks" table
 * and for exports.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickEventDto {
    private Long id;
    private Instant timestamp;
    private String ip;
    private String country;
    private String city;
    private String region;
    private String deviceType;
    private String browser;
    private String os;
    private boolean bot;
    private boolean uniqueVisit;
    private boolean suspicious;
    private String referer;
}
