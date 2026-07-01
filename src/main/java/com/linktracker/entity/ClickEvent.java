package com.linktracker.entity;

import com.linktracker.util.DeviceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * A single recorded click on an influencer's tracking link.
 * Geolocation and device fields are filled in asynchronously after the
 * redirect has already been served to the user.
 */
@Entity
@Table(name = "click_events", indexes = {
        @Index(name = "idx_click_influencer", columnList = "influencer_id"),
        @Index(name = "idx_click_timestamp", columnList = "timestamp"),
        @Index(name = "idx_click_ip", columnList = "ip"),
        @Index(name = "idx_click_influencer_ip_timestamp", columnList = "influencer_id, ip, timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "influencer_id", nullable = false)
    private Influencer influencer;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(length = 64)
    private String ip;

    private String country;
    private String countryCode;
    private String city;
    private String region;
    private String timezone;
    private String isp;
    private Double latitude;
    private Double longitude;

    @Column(name = "network_type")
    private String networkType;

    @Column(name = "is_vpn_or_proxy")
    private Boolean vpnOrProxy;

    @Column(name = "is_datacenter")
    private Boolean datacenter;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String referer;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    private DeviceType deviceType;

    private String browser;

    @Column(name = "browser_version")
    private String browserVersion;

    private String os;

    @Column(name = "os_version")
    private String osVersion;

    @Column(name = "device_manufacturer")
    private String deviceManufacturer;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "is_bot", nullable = false)
    @Builder.Default
    private boolean bot = false;

    @Column(name = "is_unique_visit", nullable = false)
    @Builder.Default
    private boolean uniqueVisit = true;

    @Column(name = "is_suspicious", nullable = false)
    @Builder.Default
    private boolean suspicious = false;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}
