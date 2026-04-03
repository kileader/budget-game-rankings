package com.kevinleader.bgr.dto.ranking;

import java.util.List;

public record RankingPageDto(
        int offset,
        int limit,
        int total,
        List<RankingResultDto> results
) {
}
