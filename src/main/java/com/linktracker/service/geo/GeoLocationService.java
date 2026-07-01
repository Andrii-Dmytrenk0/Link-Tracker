package com.linktracker.service.geo;

import com.linktracker.dto.GeoLocationResult;

/**
 * Abstraction over an IP geolocation provider. New providers (MaxMind
 * GeoLite2, ipinfo.io, etc.) can be added by implementing this interface and
 * swapping the {@code @Primary} bean in {@link com.linktracker.config.GeoConfig}
 * -- no changes to business logic required.
 */
public interface GeoLocationService {

    /**
     * Resolves geolocation data for the given IP address.
     * Implementations must never throw: on any failure they should return
     * {@link GeoLocationResult#empty()} so click recording is never blocked.
     */
    GeoLocationResult lookup(String ip);
}
