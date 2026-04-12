package com.kevinleader.bgr.dto.ranking;

import java.math.BigDecimal;
import java.util.List;

public record RankingQueryDto(
        List<Integer> platformIds,
        List<Integer> genreIds,
        Integer releaseYearMin,
        Integer releaseYearMax,
        Integer minPriceCents,
        Integer maxPriceCents,
        BigDecimal minPlaytimeHours,
        BigDecimal maxPlaytimeHours,
        RankingSort sort,
        SortDirection sortDirection,
        int offset,
        int limit
) {
}
