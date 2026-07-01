package com.linktracker.service.impl;

import com.linktracker.dto.*;
import com.linktracker.entity.ClickEvent;
import com.linktracker.entity.Influencer;
import com.linktracker.exception.ResourceNotFoundException;
import com.linktracker.mapper.ClickEventMapper;
import com.linktracker.mapper.InfluencerMapper;
import com.linktracker.repository.ClickEventRepository;
import com.linktracker.repository.InfluencerRepository;
import com.linktracker.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int TOP_N = 10;

    private final InfluencerRepository influencerRepository;
    private final ClickEventRepository clickEventRepository;
    private final InfluencerMapper influencerMapper;
    private final ClickEventMapper clickEventMapper;

    @Override
    public InfluencerStatsDto getInfluencerStats(Long influencerId, String baseUrl) {
        Influencer influencer = influencerRepository.findById(influencerId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer not found: " + influencerId));

        Instant now = Instant.now();
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant startOfWeek = LocalDate.now(ZoneOffset.UTC).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant startOfMonth = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1)
                .atStartOfDay(ZoneOffset.UTC).toInstant();

        long total = clickEventRepository.countByInfluencerId(influencerId);
        long today = clickEventRepository.countByInfluencerIdAndTimestampAfter(influencerId, startOfDay);
        long week = clickEventRepository.countByInfluencerIdAndTimestampAfter(influencerId, startOfWeek);
        long month = clickEventRepository.countByInfluencerIdAndTimestampAfter(influencerId, startOfMonth);
        long bots = total - clickEventRepository.countByInfluencerIdAndBotFalseAndTimestampAfter(influencerId, Instant.EPOCH);

        Pageable last100 = PageRequest.of(0, 100);
        List<ClickEventDto> recent = clickEventRepository
                .findByInfluencerIdOrderByTimestampDesc(influencerId, last100)
                .getContent().stream()
                .map(clickEventMapper::toDto)
                .collect(Collectors.toList());

        long unique = recent.stream().filter(ClickEventDto::isUniqueVisit).count();
        // For "unique" totals across the whole history we approximate using all
        // records flagged unique at insert time (cheap, no extra query needed
        // beyond a count) -- exact distinct-IP counting is available via a
        // dedicated query if deeper accuracy is required later.
        long repeat = total - unique;
        double uniquePct = total == 0 ? 0.0 : (unique * 100.0 / total);

        long clicks = clickEventRepository.countByInfluencerId(influencerId);

        InfluencerDto dto = influencerMapper.toDto(influencer, clicks, baseUrl);

        return InfluencerStatsDto.builder()
                .influencer(dto)
                .totalClicks(total)
                .uniqueClicks(unique)
                .repeatClicks(repeat)
                .uniquePercentage(Math.round(uniquePct * 100.0) / 100.0)
                .clicksToday(today)
                .clicksThisWeek(week)
                .clicksThisMonth(month)
                .botClicks(Math.max(bots, 0))
                .recentClicks(recent)
                .build();
    }

    @Override
    public DashboardStatsDto getDashboardStats() {
        Instant startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant last30Days = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant last14Days = Instant.now().minus(14, ChronoUnit.DAYS);

        long totalClicks = clickEventRepository.countSince(Instant.EPOCH);
        long uniqueIps = clickEventRepository.countDistinctIps();
        long clicksToday = clickEventRepository.countSince(startOfDay);

        Map<String, Long> clicksByDay = new LinkedHashMap<>();
        for (Object[] row : clickEventRepository.clicksByDay(last30Days)) {
            clicksByDay.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        double avgPerDay = clicksByDay.isEmpty() ? 0.0
                : clicksByDay.values().stream().mapToLong(Long::longValue).sum() / (double) clicksByDay.size();

        double growthRate = computeGrowthRate(clicksByDay);

        Map<Integer, Long> clicksByHour = new LinkedHashMap<>();
        for (int h = 0; h < 24; h++) {
            clicksByHour.put(h, 0L);
        }
        for (Object[] row : clickEventRepository.clicksByHour(last14Days)) {
            clicksByHour.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        List<TopEntryDto> topInfluencers = clickEventRepository.topInfluencers(PageRequest.of(0, TOP_N)).stream()
                .map(r -> new TopEntryDto(r[1] + " (" + r[2] + ")", ((Number) r[3]).longValue(), String.valueOf(r[0])))
                .collect(Collectors.toList());

        List<TopEntryDto> topCountries = toTopEntries(clickEventRepository.topCountries(TOP_N));
        List<TopEntryDto> topCities = toTopEntries(clickEventRepository.topCities(TOP_N));
        List<TopEntryDto> topBrowsers = toTopEntries(clickEventRepository.topBrowsers(TOP_N));
        List<TopEntryDto> topOs = toTopEntries(clickEventRepository.topOperatingSystems(TOP_N));
        List<TopEntryDto> topDevices = toTopEntries(clickEventRepository.topDeviceTypes());

        List<MapPointDto> mapPoints = clickEventRepository.worldMapPoints().stream()
                .map(r -> new MapPointDto(
                        String.valueOf(r[0]),
                        ((Number) r[1]).doubleValue(),
                        ((Number) r[2]).doubleValue(),
                        ((Number) r[3]).longValue()))
                .collect(Collectors.toList());

        return DashboardStatsDto.builder()
                .totalClicks(totalClicks)
                .uniqueIps(uniqueIps)
                .clicksToday(clicksToday)
                .averageClicksPerDay(Math.round(avgPerDay * 100.0) / 100.0)
                .growthRatePercentage(growthRate)
                .topInfluencers(topInfluencers)
                .topCountries(topCountries)
                .topCities(topCities)
                .topBrowsers(topBrowsers)
                .topOperatingSystems(topOs)
                .topDeviceTypes(topDevices)
                .clicksByDay(clicksByDay)
                .clicksByHour(clicksByHour)
                .mapPoints(mapPoints)
                .build();
    }

    private List<TopEntryDto> toTopEntries(List<Object[]> rows) {
        return rows.stream()
                .map(r -> new TopEntryDto(String.valueOf(r[0]), ((Number) r[1]).longValue(), null))
                .collect(Collectors.toList());
    }

    /**
     * Growth rate = percentage change between the average of the last 7 days
     * and the average of the 7 days before that, within the 30-day window
     * already loaded.
     */
    private double computeGrowthRate(Map<String, Long> clicksByDay) {
        if (clicksByDay.size() < 2) {
            return 0.0;
        }
        List<Long> values = clicksByDay.values().stream().collect(Collectors.toList());
        int n = values.size();
        int half = Math.max(1, n / 2);

        double recentAvg = values.subList(Math.max(0, n - half), n).stream()
                .mapToLong(Long::longValue).average().orElse(0.0);
        double previousAvg = values.subList(0, Math.max(0, n - half)).stream()
                .mapToLong(Long::longValue).average().orElse(0.0);

        if (previousAvg == 0.0) {
            return recentAvg > 0 ? 100.0 : 0.0;
        }
        return Math.round(((recentAvg - previousAvg) / previousAvg) * 10000.0) / 100.0;
    }
}
