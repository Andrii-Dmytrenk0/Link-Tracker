package com.linktracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Aggregated statistics shown on the admin dashboard home page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalClicks;
    private long uniqueIps;
    private long clicksToday;
    private double averageClicksPerDay;
    private double growthRatePercentage;
    private List<TopEntryDto> topInfluencers;
    private List<TopEntryDto> topCountries;
    private List<TopEntryDto> topCities;
    private List<TopEntryDto> topBrowsers;
    private List<TopEntryDto> topOperatingSystems;
    private List<TopEntryDto> topDeviceTypes;
    private Map<String, Long> clicksByDay;
    private Map<Integer, Long> clicksByHour;
    private List<MapPointDto> mapPoints;
}
