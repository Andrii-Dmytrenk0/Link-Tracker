package com.linktracker.mapper;

import com.linktracker.dto.InfluencerDto;
import com.linktracker.entity.Influencer;
import org.springframework.stereotype.Component;

/**
 * Converts between {@link Influencer} entities and {@link InfluencerDto}.
 */
@Component
public class InfluencerMapper {

    public InfluencerDto toDto(Influencer entity, long totalClicks, String baseUrl) {
        return InfluencerDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .instagramUrl(entity.getInstagramUrl())
                .trackingUrl(baseUrl + "/i/" + entity.getCode())
                .active(entity.isActive())
                .totalClicks(totalClicks)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
