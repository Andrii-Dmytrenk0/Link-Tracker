package com.linktracker.service;

import com.linktracker.dto.InfluencerCreateRequest;
import com.linktracker.dto.InfluencerDto;
import com.linktracker.dto.InfluencerUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Business operations for managing influencers (full CRUD plus short-code
 * generation and activation toggling).
 */
public interface InfluencerService {

    InfluencerDto create(InfluencerCreateRequest request, String baseUrl);

    InfluencerDto update(Long id, InfluencerUpdateRequest request, String baseUrl);

    void delete(Long id);

    InfluencerDto getById(Long id, String baseUrl);

    Optional<com.linktracker.entity.Influencer> findActiveByCode(String code);

    Page<InfluencerDto> search(String search, Pageable pageable, String baseUrl);

    InfluencerDto toggleActive(Long id, String baseUrl);

    /** Generates a brand-new unique short code for an existing influencer. */
    InfluencerDto regenerateCode(Long id, String baseUrl);

    /** Generates a unique short code not currently assigned to any influencer. */
    String generateUniqueCode();
}
