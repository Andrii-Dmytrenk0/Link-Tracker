package com.linktracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Normalized result returned by any {@link com.linktracker.service.geo.GeoLocationService}
 * implementation, decoupling callers from the specific provider (ip-api.com,
 * MaxMind GeoLite2, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeoLocationResult {
    private String country;
    private String countryCode;
    private String city;
    private String region;
    private String timezone;
    private String isp;
    private Double latitude;
    private Double longitude;
    private String networkType;
    private Boolean vpnOrProxy;
    private Boolean datacenter;

    public static GeoLocationResult empty() {
        return GeoLocationResult.builder().build();
    }
}
