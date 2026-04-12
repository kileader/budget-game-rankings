package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.ranking.RankingPageDto;
import com.kevinleader.bgr.dto.ranking.RankingQueryDto;
import com.kevinleader.bgr.dto.ranking.RankingResultDto;
import com.kevinleader.bgr.dto.ranking.RankingSort;
import com.kevinleader.bgr.dto.ranking.SortDirection;
import com.kevinleader.bgr.entity.GameCache;
import com.kevinleader.bgr.repository.GameCacheRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class RankingService {

    private final GameCacheRepository gameCacheRepository;

    public RankingService(GameCacheRepository gameCacheRepository) {
        this.gameCacheRepository = gameCacheRepository;
    }

    public List<RankingResultDto> getTopRankings(int limit) {
        return getRankingsPage(new RankingQueryDto(
                null, null, null, null, null, null, null, null,
                RankingSort.VALUE_SCORE, SortDirection.DESC, 0, limit
        )).results();
    }

    public RankingPageDto getRankingsPage(RankingQueryDto query) {
        validateQuery(query);
        int safeLimit = query.limit();
        int safeOffset = query.offset();
        Comparator<GameCache> comparator = buildComparator(query.sort(), query.sortDirection());

        List<GameCache> rankedGames = gameCacheRepository.findAllRankable().stream()
                .filter(game -> matchesFilters(game, query))
                .sorted(comparator)
                .toList();

        List<RankingResultDto> paged = rankedGames.stream()
                .skip(safeOffset)
                .limit(safeLimit)
                .map(this::toRankingResult)
                .toList();

        return new RankingPageDto(safeOffset, safeLimit, rankedGames.size(), paged);
    }

    private void validateQuery(RankingQueryDto query) {
        if (query.offset() < 0) {
            throw new IllegalArgumentException("offset must be at least 0");
        }
        if (query.limit() < 1 || query.limit() > 500) {
            throw new IllegalArgumentException("limit must be between 1 and 500");
        }
        if (query.releaseYearMin() != null && query.releaseYearMax() != null
                && query.releaseYearMin() > query.releaseYearMax()) {
            throw new IllegalArgumentException("releaseYearMin must be less than or equal to releaseYearMax");
        }
        if (query.minPriceCents() != null && query.maxPriceCents() != null
                && query.minPriceCents() > query.maxPriceCents()) {
            throw new IllegalArgumentException("minPriceCents must be less than or equal to maxPriceCents");
        }
        if (query.minPlaytimeHours() != null && query.maxPlaytimeHours() != null
                && query.minPlaytimeHours().compareTo(query.maxPlaytimeHours()) > 0) {
            throw new IllegalArgumentException("minPlaytimeHours must be less than or equal to maxPlaytimeHours");
        }
    }

    private Comparator<GameCache> buildComparator(RankingSort sort, SortDirection direction) {
        RankingSort safeSort = sort == null ? RankingSort.VALUE_SCORE : sort;
        boolean desc = direction == null || direction == SortDirection.DESC;
        // Always show highest value score among ties regardless of primary sort direction
        Comparator<GameCache> tiebreak = Comparator.comparing(this::computeValueScore, Comparator.reverseOrder());

        return switch (safeSort) {
            case RATING -> {
                Comparator<GameCache> c = Comparator.comparing(GameCache::getIgdbRating);
                yield (desc ? c.reversed() : c).thenComparing(tiebreak);
            }
            case PLAYTIME -> {
                Comparator<GameCache> c = Comparator.comparing(GameCache::getHltbHours);
                yield (desc ? c.reversed() : c).thenComparing(tiebreak);
            }
            case PRICE -> {
                Comparator<GameCache> c = Comparator.comparing(GameCache::getEffectivePriceCents);
                yield (desc ? c.reversed() : c).thenComparing(tiebreak);
            }
            case TITLE -> {
                Comparator<GameCache> c = Comparator.comparing(
                        (GameCache game) -> game.getTitle().toLowerCase(Locale.ROOT));
                yield (desc ? c.reversed() : c).thenComparing(tiebreak);
            }
            case RELEASE_DATE -> Comparator.comparing(GameCache::getFirstReleaseDate,
                            desc ? Comparator.nullsLast(Comparator.reverseOrder())
                                 : Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(tiebreak);
            case VALUE_SCORE -> {
                Comparator<GameCache> c = Comparator.comparing(this::computeValueScore)
                        .thenComparing(GameCache::getIgdbRating, Comparator.reverseOrder());
                yield desc ? c.reversed() : c;
            }
        };
    }

    private boolean matchesFilters(GameCache game, RankingQueryDto query) {
        Integer priceCents = game.getEffectivePriceCents();
        if (priceCents == null) {
            return false;
        }

        if (query.platformIds() != null && !query.platformIds().isEmpty()
                && !overlaps(game.getPlatformIds(), query.platformIds())) {
            return false;
        }

        if (query.genreIds() != null && !query.genreIds().isEmpty()
                && !overlaps(game.getGenreIds(), query.genreIds())) {
            return false;
        }

        if (query.releaseYearMin() != null
                && (game.getFirstReleaseDate() == null || game.getFirstReleaseDate().getYear() < query.releaseYearMin())) {
            return false;
        }

        if (query.releaseYearMax() != null
                && (game.getFirstReleaseDate() == null || game.getFirstReleaseDate().getYear() > query.releaseYearMax())) {
            return false;
        }

        if (query.minPriceCents() != null && priceCents < query.minPriceCents()) {
            return false;
        }

        if (query.maxPriceCents() != null && priceCents > query.maxPriceCents()) {
            return false;
        }

        if (query.minPlaytimeHours() != null && game.getHltbHours().compareTo(query.minPlaytimeHours()) < 0) {
            return false;
        }

        if (query.maxPlaytimeHours() != null && game.getHltbHours().compareTo(query.maxPlaytimeHours()) > 0) {
            return false;
        }

        return true;
    }

    private boolean overlaps(int[] values, List<Integer> requested) {
        if (values == null || values.length == 0) {
            return false;
        }

        Set<Integer> requestedSet = requested.stream().collect(Collectors.toSet());
        return IntStream.of(values).anyMatch(requestedSet::contains);
    }

    private RankingResultDto toRankingResult(GameCache game) {
        Integer priceCents = game.getEffectivePriceCents();
        if (priceCents == null || priceCents <= 0) {
            throw new IllegalStateException("Rankable game missing usable price: " + game.getIgdbGameId());
        }

        BigDecimal valueScore = computeValueScore(game);

        return new RankingResultDto(
                game.getIgdbGameId(),
                game.getTitle(),
                game.getIgdbRating(),
                game.getHltbHours(),
                priceCents,
                valueScore,
                game.getCoverImageUrl(),
                game.getIgdbUrl(),
                game.getCheapsharkDealUrl()
        );
    }

    private BigDecimal computeValueScore(GameCache game) {
        Integer priceCents = game.getEffectivePriceCents();
        if (priceCents == null || priceCents <= 0) {
            throw new IllegalStateException("Rankable game missing usable price: " + game.getIgdbGameId());
        }

        BigDecimal priceDollars = BigDecimal.valueOf(priceCents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return game.getIgdbRating()
                .multiply(game.getHltbHours())
                .divide(priceDollars, 4, RoundingMode.HALF_UP);
    }
}
