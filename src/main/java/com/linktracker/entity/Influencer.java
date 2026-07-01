package com.linktracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Represents an influencer who owns a unique tracking link
 * ({@code https://mydomain.com/i/{code}}) that redirects to their
 * Instagram shop.
 */
@Entity
@Table(name = "influencers", indexes = {
        @Index(name = "idx_influencer_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Influencer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 16)
    private String code;

    @Column(name = "instagram_url", nullable = false, length = 500)
    private String instagramUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
