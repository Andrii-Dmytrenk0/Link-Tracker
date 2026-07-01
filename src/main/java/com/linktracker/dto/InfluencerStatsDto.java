package com.linktracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Detailed statistics for a single influencer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfluencerStatsDto {
    private InfluencerDto influencer;
    private long totalClicks;
    private long uniqueClicks;
    private long repeatClicks;
    private double uniquePercentage;
    private long clicksToday;
    private long clicksThisWeek;
    private long clicksThisMonth;
    private long botClicks;
    private List<ClickEventDto> recentClicks;
}
