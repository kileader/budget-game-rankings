package com.kevinleader.bgr.dto.config;

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
        BigDecimal maxPlaytimeHours
) {
}