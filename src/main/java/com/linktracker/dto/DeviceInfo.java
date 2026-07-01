package com.linktracker.dto;

import com.linktracker.util.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Normalized result of parsing a User-Agent header.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {
    private String browser;
    private String browserVersion;
    private String os;
    private String osVersion;
    private DeviceType deviceType;
    private String deviceManufacturer;
    private String deviceModel;
    private boolean bot;
}
