package com.linktracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single point used to render the world click-map on the dashboard.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MapPointDto {
    private String country;
    private double latitude;
    private double longitude;
    private long count;
}
