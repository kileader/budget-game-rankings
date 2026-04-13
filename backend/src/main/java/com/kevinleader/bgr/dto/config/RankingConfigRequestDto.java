package com.kevinleader.bgr.dto.config;

import com.kevinleader.bgr.dto.ranking.ScoringWeightConstraints;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record RankingConfigRequestDto(
        @NotBlank
        @Size(max = 100)
        String name,

        List<Integer> platformIds,
        List<Integer> genreIds,
        Integer releaseYearMin,
        Integer releaseYearMax,
        Integer minPriceCents,
        Integer maxPriceCents,
        BigDecimal minPlaytimeHours,
        BigDecimal maxPlaytimeHours,
        @DecimalMin(value = ScoringWeightConstraints.MIN_STR, inclusive = true)
        @DecimalMax(value = ScoringWeightConstraints.MAX_STR, inclusive = true)
        BigDecimal ratingWeight,
        @DecimalMin(value = ScoringWeightConstraints.MIN_STR, inclusive = true)
        @DecimalMax(value = ScoringWeightConstraints.MAX_STR, inclusive = true)
        BigDecimal playtimeWeight,
        @DecimalMin(value = ScoringWeightConstraints.MIN_STR, inclusive = true)
        @DecimalMax(value = ScoringWeightConstraints.MAX_STR, inclusive = true)
        BigDecimal priceWeight
) {
}