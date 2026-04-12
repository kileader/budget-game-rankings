package com.kevinleader.bgr.controller;

import com.kevinleader.bgr.dto.ranking.RankingPageDto;
import com.kevinleader.bgr.dto.ranking.RankingQueryDto;
import com.kevinleader.bgr.dto.ranking.RankingSort;
import com.kevinleader.bgr.dto.ranking.SortDirection;
import com.kevinleader.bgr.service.RankingService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

@Validated
@RestController
@RequestMapping("/rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public RankingPageDto getRankings(
            @RequestParam(required = false) List<Integer> platformIds,
            @RequestParam(required = false) List<Integer> genreIds,
            @RequestParam(required = false) Integer releaseYearMin,
            @RequestParam(required = false) Integer releaseYearMax,
            @RequestParam(required = false) Integer minPriceCents,
            @RequestParam(required = false) Integer maxPriceCents,
            @RequestParam(required = false) BigDecimal minPlaytimeHours,
            @RequestParam(required = false) BigDecimal maxPlaytimeHours,
            @RequestParam(defaultValue = "VALUE_SCORE") RankingSort sort,
            @RequestParam(defaultValue = "DESC") SortDirection sortDirection,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "100") @Min(1) @Max(500) int limit) {
        return rankingService.getRankingsPage(new RankingQueryDto(
                platformIds,
                genreIds,
                releaseYearMin,
                releaseYearMax,
                minPriceCents,
                maxPriceCents,
                minPlaytimeHours,
                maxPlaytimeHours,
                sort,
                sortDirection,
                offset,
                limit
        ));
    }
}
