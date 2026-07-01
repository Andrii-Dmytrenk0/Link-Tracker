package com.linktracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic "label -> count" pair used for top-N lists (countries, browsers, etc.).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopEntryDto {
    private String label;
    private long count;
    private String extra; // e.g. influencer code, optional
}
