package com.linktracker.mapper;

import com.linktracker.dto.ClickEventDto;
import com.linktracker.entity.ClickEvent;
import org.springframework.stereotype.Component;

/**
 * Converts {@link ClickEvent} entities into {@link ClickEventDto}.
 */
@Component
public class ClickEventMapper {

    public ClickEventDto toDto(ClickEvent event) {
        return ClickEventDto.builder()
                .id(event.getId())
                .timestamp(event.getTimestamp())
                .ip(event.getIp())
                .country(event.getCountry())
                .city(event.getCity())
                .region(event.getRegion())
                .deviceType(event.getDeviceType() != null ? event.getDeviceType().name() : null)
                .browser(event.getBrowser())
                .os(event.getOs())
                .bot(event.isBot())
                .uniqueVisit(event.isUniqueVisit())
                .suspicious(event.isSuspicious())
                .referer(event.getReferer())
                .build();
    }
}
