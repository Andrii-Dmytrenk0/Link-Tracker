package com.linktracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Payload for updating an existing influencer.
 */
@Data
public class InfluencerUpdateRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^[A-Za-z0-9_-]{4,16}$", message = "Code must be 4-16 letters, digits, '-' or '_'")
    private String code;

    @NotBlank(message = "Instagram URL is required")
    @Size(max = 500)
    private String instagramUrl;

    private boolean active = true;
}
