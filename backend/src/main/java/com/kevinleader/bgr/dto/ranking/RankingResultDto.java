package com.kevinleader.bgr.dto.ranking;

import java.math.BigDecimal;

public record RankingResultDto(
        Long igdbGameId,
        String title,
        BigDecimal igdbRating,
        BigDecimal hltbHours,
        Integer priceCents,
        BigDecimal valueScore,
        String coverImageUrl,
        String igdbUrl,
        String cheapsharkDealUrl
) {
}
