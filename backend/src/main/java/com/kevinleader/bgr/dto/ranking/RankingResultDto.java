package com.kevinleader.bgr.dto.ranking;

import java.math.BigDecimal;

/**
 * Ranking row returned by the API.
 *
 * @param priceIsTrackedDeal {@code true} when {@code priceCents} comes from CheapShark (tracked deal);
 *                           {@code false} when estimate or nominal free substitute.
 */
public record RankingResultDto(
        Long igdbGameId,
        String title,
        BigDecimal igdbRating,
        BigDecimal hltbHours,
        boolean hltbFound,
        Integer priceCents,
        boolean priceIsTrackedDeal,
        BigDecimal valueScore,
        String coverImageUrl,
        String igdbUrl,
        String cheapsharkDealUrl,
        Integer steamAppId,
        int[] platformIds,
        String ageRatingDisplay
) {
}
