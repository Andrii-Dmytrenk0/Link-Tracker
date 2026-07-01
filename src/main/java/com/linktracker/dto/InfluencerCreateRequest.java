package com.linktracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Payload for creating a new influencer. If {@code code} is left blank the
 * server will auto-generate a unique short code.
 */
@Data
public class InfluencerCreateRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    @Pattern(regexp = "^[A-Za-z0-9_-]{0,16}$", message = "Code may only contain letters, digits, '-' and '_'")
    private String code;

    @NotBlank(message = "Instagram URL is required")
    @Size(max = 500)
    private String instagramUrl;

    private boolean active = true;
}
