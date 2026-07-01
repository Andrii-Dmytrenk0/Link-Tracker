package com.linktracker.controller.api;

import com.linktracker.dto.DashboardStatsDto;
import com.linktracker.dto.InfluencerStatsDto;
import com.linktracker.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for reading statistics: GET /api/stats (global dashboard) and
 * GET /api/stats/{id} (per-influencer).
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Dashboard and per-influencer analytics")
public class StatsRestController {

    private final StatsService statsService;

    @GetMapping
    @Operation(summary = "Global dashboard statistics")
    public DashboardStatsDto dashboard() {
        return statsService.getDashboardStats();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detailed statistics for one influencer")
    public InfluencerStatsDto influencerStats(@PathVariable Long id) {
        return statsService.getInfluencerStats(id, "");
    }
}
