package com.linktracker.controller.api;

import com.linktracker.dto.InfluencerCreateRequest;
import com.linktracker.dto.InfluencerDto;
import com.linktracker.dto.InfluencerUpdateRequest;
import com.linktracker.service.InfluencerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for managing influencers.
 * GET /api/influencers, POST /api/influencers, DELETE /api/influencers/{id}, plus
 * additional convenience endpoints (toggle, regenerate code) used by the admin UI.
 */
@RestController
@RequestMapping("/api/influencers")
@RequiredArgsConstructor
@Tag(name = "Influencers", description = "CRUD operations for influencers")
public class InfluencerRestController {

    private final InfluencerService influencerService;

    @GetMapping
    @Operation(summary = "List / search influencers (paginated)")
    public Page<InfluencerDto> list(@RequestParam(defaultValue = "") String search,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     @RequestParam(defaultValue = "createdAt") String sortBy,
                                     @RequestParam(defaultValue = "desc") String direction,
                                     HttpServletRequest request) {
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return influencerService.search(search, PageRequest.of(page, size, Sort.by(dir, sortBy)), baseUrl(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single influencer by id")
    public InfluencerDto getOne(@PathVariable Long id, HttpServletRequest request) {
        return influencerService.getById(id, baseUrl(request));
    }

    @PostMapping
    @Operation(summary = "Create a new influencer (auto-generates a code if none supplied)")
    public ResponseEntity<InfluencerDto> create(@Valid @RequestBody InfluencerCreateRequest body,
                                                 HttpServletRequest request) {
        InfluencerDto created = influencerService.create(body, baseUrl(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing influencer")
    public InfluencerDto update(@PathVariable Long id, @Valid @RequestBody InfluencerUpdateRequest body,
                                 HttpServletRequest request) {
        return influencerService.update(id, body, baseUrl(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an influencer")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        influencerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    @Operation(summary = "Enable or disable an influencer's tracking link")
    public InfluencerDto toggleActive(@PathVariable Long id, HttpServletRequest request) {
        return influencerService.toggleActive(id, baseUrl(request));
    }

    @PostMapping("/{id}/regenerate-code")
    @Operation(summary = "Generate a new random short code for this influencer")
    public InfluencerDto regenerateCode(@PathVariable Long id, HttpServletRequest request) {
        return influencerService.regenerateCode(id, baseUrl(request));
    }

    private String baseUrl(HttpServletRequest request) {
        String scheme = request.getHeader("X-Forwarded-Proto") != null
                ? request.getHeader("X-Forwarded-Proto") : request.getScheme();
        String host = request.getHeader("X-Forwarded-Host") != null
                ? request.getHeader("X-Forwarded-Host") : request.getServerName();
        return scheme + "://" + host;
    }
}
