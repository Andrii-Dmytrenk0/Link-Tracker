package com.linktracker.service.impl;

import com.linktracker.dto.DeviceInfo;
import com.linktracker.dto.GeoLocationResult;
import com.linktracker.entity.ClickEvent;
import com.linktracker.entity.Influencer;
import com.linktracker.repository.ClickEventRepository;
import com.linktracker.service.BotDetectionService;
import com.linktracker.service.UserAgentParsingService;
import com.linktracker.service.geo.GeoLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Performs the actual GeoIP lookup, User-Agent parsing and persistence of a
 * click event. Runs entirely on the {@code clickTrackingExecutor} thread
 * pool (see {@link com.linktracker.config.AsyncConfig}) so the HTTP redirect
 * response returned to the user is never delayed by it.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClickEnrichmentService {

    private final ClickEventRepository clickEventRepository;
    private final GeoLocationService geoLocationService;
    private final UserAgentParsingService userAgentParsingService;
    private final BotDetectionService botDetectionService;

    @Async("clickTrackingExecutor")
    public void enrichAndPersist(Long influencerId, String ip, String userAgent, String referer) {
        try {
            DeviceInfo deviceInfo = userAgentParsingService.parse(userAgent);
            GeoLocationResult geo = geoLocationService.lookup(ip);

            boolean repeatRecently = !deviceInfo.isBot()
                    && clickEventRepository.countRecentClicksFromIp(
                            influencerId, ip, Instant.now().minus(30, ChronoUnit.DAYS)) > 0;
            boolean suspicious = deviceInfo.isBot()
                    || botDetectionService.registerAndCheckSuspicious(influencerId, ip);

            Influencer ref = new Influencer();
            ref.setId(influencerId);

            ClickEvent event = ClickEvent.builder()
                    .influencer(ref)
                    .timestamp(Instant.now())
                    .ip(ip)
                    .userAgent(userAgent)
                    .referer(referer)
                    .country(geo.getCountry())
                    .countryCode(geo.getCountryCode())
                    .city(geo.getCity())
                    .region(geo.getRegion())
                    .timezone(geo.getTimezone())
                    .isp(geo.getIsp())
                    .latitude(geo.getLatitude())
                    .longitude(geo.getLongitude())
                    .networkType(geo.getNetworkType())
                    .vpnOrProxy(geo.getVpnOrProxy())
                    .datacenter(geo.getDatacenter())
                    .browser(deviceInfo.getBrowser())
                    .browserVersion(deviceInfo.getBrowserVersion())
                    .os(deviceInfo.getOs())
                    .osVersion(deviceInfo.getOsVersion())
                    .deviceManufacturer(deviceInfo.getDeviceManufacturer())
                    .deviceModel(deviceInfo.getDeviceModel())
                    .deviceType(deviceInfo.getDeviceType())
                    .bot(deviceInfo.isBot())
                    .uniqueVisit(!repeatRecently)
                    .suspicious(suspicious)
                    .build();

            clickEventRepository.save(event);
        } catch (Exception ex) {
            // Click tracking must never break the user experience; a failed
            // enrichment simply means this one click is not recorded.
            log.warn("Failed to record click for influencer {}: {}", influencerId, ex.getMessage());
        }
    }
}
