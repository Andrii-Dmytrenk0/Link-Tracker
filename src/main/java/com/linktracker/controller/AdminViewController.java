package com.linktracker.controller;

import com.linktracker.dto.InfluencerDto;
import com.linktracker.service.InfluencerService;
import com.linktracker.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Server-rendered (Thymeleaf) admin panel: dashboard, influencer list with
 * search/sort/pagination, and per-influencer statistics.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final InfluencerService influencerService;
    private final StatsService statsService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", statsService.getDashboardStats());
        return "dashboard";
    }

    @GetMapping("/influencers")
    public String influencers(@RequestParam(defaultValue = "") String search,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(defaultValue = "createdAt") String sortBy,
                               @RequestParam(defaultValue = "desc") String direction,
                               HttpServletRequest request,
                               Model model) {
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String baseUrl = baseUrl(request);

        model.addAttribute("influencers",
                influencerService.search(search, PageRequest.of(page, size, Sort.by(dir, sortBy)), baseUrl));
        model.addAttribute("search", search);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);
        return "influencers";
    }

    @GetMapping("/influencers/{id}")
    public String influencerStats(@PathVariable Long id, Model model) {
        model.addAttribute("stats", statsService.getInfluencerStats(id, ""));
        return "influencer-stats";
    }

    @GetMapping("/influencers/new")
    public String newInfluencerForm(Model model) {
        model.addAttribute("influencer", new InfluencerDto());
        model.addAttribute("isNew", true);
        return "influencer-form";
    }

    @GetMapping("/influencers/{id}/edit")
    public String editInfluencerForm(@PathVariable Long id, HttpServletRequest request, Model model) {
        model.addAttribute("influencer", influencerService.getById(id, baseUrl(request)));
        model.addAttribute("isNew", false);
        return "influencer-form";
    }

    private String baseUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto") != null
                ? request.getHeader("X-Forwarded-Proto") : request.getScheme();
        String host = request.getHeader("X-Forwarded-Host") != null
                ? request.getHeader("X-Forwarded-Host") : request.getServerName();
        return scheme + "://" + host;
    }
}
