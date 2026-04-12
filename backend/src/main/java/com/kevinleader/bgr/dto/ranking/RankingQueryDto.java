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
        String title,
        BigDecimal ratingWeight,
        BigDecimal playtimeWeight,
        BigDecimal priceWeight,
        boolean includeFreeToPlay,
        boolean includeMultiplayerOnly,
        RankingSort sort,
        SortDirection sortDirection,
        int offset,
        int limit
) {
    public BigDecimal effectiveRatingWeight()   { return ratingWeight   != null ? ratingWeight   : BigDecimal.ONE; }
    public BigDecimal effectivePlaytimeWeight() { return playtimeWeight != null ? playtimeWeight : BigDecimal.ONE; }
    public BigDecimal effectivePriceWeight()    { return priceWeight    != null ? priceWeight    : BigDecimal.ONE; }
}
