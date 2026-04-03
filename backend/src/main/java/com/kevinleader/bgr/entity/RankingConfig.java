package com.kevinleader.bgr.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ranking_config")
@Getter @Setter @NoArgsConstructor
public class RankingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "platform_ids", columnDefinition = "integer[]")
    private int[] platformIds = new int[0];

    @Column(name = "genre_ids", columnDefinition = "integer[]")
    private int[] genreIds = new int[0];

    @Column(name = "release_year_min")
    private Integer releaseYearMin;

    @Column(name = "release_year_max")
    private Integer releaseYearMax;

    @Column(name = "min_price_cents", nullable = false)
    private int minPriceCents = 0;

    @Column(name = "max_price_cents")
    private Integer maxPriceCents;

    @Column(name = "min_playtime_hours", precision = 6, scale = 2)
    private BigDecimal minPlaytimeHours;

    @Column(name = "max_playtime_hours", precision = 6, scale = 2)
    private BigDecimal maxPlaytimeHours;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
