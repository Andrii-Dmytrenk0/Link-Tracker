package com.linktracker.service;

import com.linktracker.dto.DashboardStatsDto;
import com.linktracker.dto.InfluencerStatsDto;

/**
 * Read-only analytics: per-influencer statistics and the global admin
 * dashboard summary.
 */
public interface StatsService {

    InfluencerStatsDto getInfluencerStats(Long influencerId, String baseUrl);

    DashboardStatsDto getDashboardStats();
}
