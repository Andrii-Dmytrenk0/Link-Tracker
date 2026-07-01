package com.linktracker.service.impl;

import com.linktracker.dto.InfluencerCreateRequest;
import com.linktracker.dto.InfluencerDto;
import com.linktracker.dto.InfluencerUpdateRequest;
import com.linktracker.entity.Influencer;
import com.linktracker.exception.DuplicateCodeException;
import com.linktracker.exception.ResourceNotFoundException;
import com.linktracker.mapper.InfluencerMapper;
import com.linktracker.repository.ClickEventRepository;
import com.linktracker.repository.InfluencerRepository;
import com.linktracker.service.InfluencerService;
import com.linktracker.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InfluencerServiceImpl implements InfluencerService {

    private static final int MAX_CODE_GENERATION_ATTEMPTS = 10;

    private final InfluencerRepository influencerRepository;
    private final ClickEventRepository clickEventRepository;
    private final InfluencerMapper influencerMapper;

    @Override
    public InfluencerDto create(InfluencerCreateRequest request, String baseUrl) {
        String code = (request.getCode() == null || request.getCode().isBlank())
                ? generateUniqueCode()
                : normalizeAndValidateCode(request.getCode());

        Influencer influencer = Influencer.builder()
                .name(request.getName())
                .code(code)
                .instagramUrl(request.getInstagramUrl())
                .active(request.isActive())
                .build();

        influencer = influencerRepository.save(influencer);
        log.info("Created influencer '{}' with code '{}'", influencer.getName(), influencer.getCode());
        return influencerMapper.toDto(influencer, 0L, baseUrl);
    }

    @Override
    public InfluencerDto update(Long id, InfluencerUpdateRequest request, String baseUrl) {
        Influencer influencer = getOrThrow(id);

        if (!influencer.getCode().equalsIgnoreCase(request.getCode())
                && influencerRepository.existsByCode(request.getCode())) {
            throw new DuplicateCodeException("Code '" + request.getCode() + "' is already in use");
        }

        influencer.setName(request.getName());
        influencer.setCode(request.getCode());
        influencer.setInstagramUrl(request.getInstagramUrl());
        influencer.setActive(request.isActive());

        influencer = influencerRepository.save(influencer);
        long clicks = clickEventRepository.countByInfluencerId(influencer.getId());
        return influencerMapper.toDto(influencer, clicks, baseUrl);
    }

    @Override
    public void delete(Long id) {
        if (!influencerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Influencer not found: " + id);
        }
        influencerRepository.deleteById(id);
        log.info("Deleted influencer {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public InfluencerDto getById(Long id, String baseUrl) {
        Influencer influencer = getOrThrow(id);
        long clicks = clickEventRepository.countByInfluencerId(id);
        return influencerMapper.toDto(influencer, clicks, baseUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Influencer> findActiveByCode(String code) {
        return influencerRepository.findByCode(code).filter(Influencer::isActive);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InfluencerDto> search(String search, Pageable pageable, String baseUrl) {
        Page<Influencer> page = influencerRepository.search(search, pageable);
        return page.map(i -> influencerMapper.toDto(i, clickEventRepository.countByInfluencerId(i.getId()), baseUrl));
    }

    @Override
    public InfluencerDto toggleActive(Long id, String baseUrl) {
        Influencer influencer = getOrThrow(id);
        influencer.setActive(!influencer.isActive());
        influencer = influencerRepository.save(influencer);
        long clicks = clickEventRepository.countByInfluencerId(id);
        return influencerMapper.toDto(influencer, clicks, baseUrl);
    }

    @Override
    public InfluencerDto regenerateCode(Long id, String baseUrl) {
        Influencer influencer = getOrThrow(id);
        influencer.setCode(generateUniqueCode());
        influencer = influencerRepository.save(influencer);
        long clicks = clickEventRepository.countByInfluencerId(id);
        return influencerMapper.toDto(influencer, clicks, baseUrl);
    }

    @Override
    public String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            String candidate = ShortCodeGenerator.generate();
            if (!influencerRepository.existsByCode(candidate)) {
                return candidate;
            }
        }
        // Extremely unlikely fallback: widen the alphabet space.
        String candidate = ShortCodeGenerator.generate(8);
        if (influencerRepository.existsByCode(candidate)) {
            throw new IllegalStateException("Unable to generate a unique short code, please retry");
        }
        return candidate;
    }

    private Influencer getOrThrow(Long id) {
        return influencerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer not found: " + id));
    }

    private String normalizeAndValidateCode(String code) {
        if (influencerRepository.existsByCode(code)) {
            throw new DuplicateCodeException("Code '" + code + "' is already in use");
        }
        return code;
    }
}
