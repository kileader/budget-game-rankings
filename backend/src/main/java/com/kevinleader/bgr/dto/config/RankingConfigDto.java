package com.kevinleader.bgr.dto.config;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record RankingConfigDto(
        Long id,
        String name,
        List<Integer> platformIds,
        List<Integer> genreIds,
        Integer releaseYearMin,
        Integer releaseYearMax,
        Integer minPriceCents,
        Integer maxPriceCents,
        BigDecimal minPlaytimeHours,
        BigDecimal maxPlaytimeHours,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
