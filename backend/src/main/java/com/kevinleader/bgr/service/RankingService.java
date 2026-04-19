package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.ranking.RankingPageDto;
import com.kevinleader.bgr.dto.ranking.RankingQueryDto;
import com.kevinleader.bgr.dto.ranking.ScoringWeightConstraints;
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
import java.util.Objects;
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
                null, null, null, null, null, null, null, null, null,
                null, null, null, false, false, false,
                RankingSort.VALUE_SCORE, SortDirection.DESC, 0, limit
        )).results();
    }

    public RankingPageDto getRankingsPage(RankingQueryDto query) {
        validateQuery(query);
        int safeLimit = query.limit();
        int safeOffset = query.offset();

        boolean inclusive = query.includeFreeToPlay() || query.includeMultiplayerOnly();
        List<GameCache> source = inclusive
                ? gameCacheRepository.findAllForRanking(query.includeFreeToPlay(), query.includeMultiplayerOnly())
                : gameCacheRepository.findAllRankable();

        Comparator<GameCache> comparator = buildComparator(query);

        List<GameCache> rankedGames = source.stream()
                .filter(game -> matchesFilters(game, query))
                .sorted(comparator)
                .toList();

        List<RankingResultDto> paged = rankedGames.stream()
                .skip(safeOffset)
                .limit(safeLimit)
                .map(game -> toRankingResult(game, query))
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
        validateWeight("ratingWeight", query.ratingWeight());
        validateWeight("playtimeWeight", query.playtimeWeight());
        validateWeight("priceWeight", query.priceWeight());
    }

    private static void validateWeight(String name, BigDecimal weight) {
        if (weight == null) {
            return;
        }
        if (weight.compareTo(ScoringWeightConstraints.MIN) < 0
                || weight.compareTo(ScoringWeightConstraints.MAX) > 0) {
            throw new IllegalArgumentException(
                    name + " must be between " + ScoringWeightConstraints.MIN_STR
                            + " and " + ScoringWeightConstraints.MAX_STR + " inclusive");
        }
    }

    private Comparator<GameCache> buildComparator(RankingQueryDto query) {
        RankingSort safeSort = query.sort() == null ? RankingSort.VALUE_SCORE : query.sort();
        boolean desc = query.sortDirection() == null || query.sortDirection() == SortDirection.DESC;
        Comparator<GameCache> tiebreak = Comparator.comparing(
                (GameCache g) -> computeValueScore(g, query), Comparator.reverseOrder());

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
                Comparator<GameCache> c = Comparator.comparing(
                        (GameCache g) -> effectivePriceCents(g, query.includeFreeToPlay()));
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
                Comparator<GameCache> c = Comparator.comparing(
                                (GameCache g) -> computeValueScore(g, query))
                        .thenComparing(GameCache::getIgdbRating, Comparator.reverseOrder());
                yield desc ? c.reversed() : c;
            }
        };
    }

    private boolean matchesFilters(GameCache game, RankingQueryDto query) {
        if (game.isFree() && !query.includeFreeToPlay()) {
            return false;
        }
        if (game.isMultiplayerOnly() && !query.includeMultiplayerOnly()) {
            return false;
        }

        Integer priceCents = effectivePriceCents(game, query.includeFreeToPlay());
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

        if (query.title() != null && !query.title().isBlank()
                && !game.getTitle().toLowerCase(Locale.ROOT).contains(query.title().toLowerCase(Locale.ROOT))) {
            return false;
        }

        if (query.excludeAdultRated() && AdultAgeRatingClassifier.isAdultRated(game.getAgeRatingDisplay())) {
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

    private RankingResultDto toRankingResult(GameCache game, RankingQueryDto query) {
        Integer priceCents = effectivePriceCents(game, query.includeFreeToPlay());
        BigDecimal valueScore = computeValueScore(game, query);

        int[] platformIds = game.getPlatformIds() != null ? game.getPlatformIds() : new int[0];
        Integer cheapsharkCents = game.getCheapsharkPriceCents();
        boolean showingCheapsharkPrice = cheapsharkCents != null
                && !(game.isFree() && query.includeFreeToPlay())
                && Objects.equals(priceCents, cheapsharkCents);
        boolean priceIsTrackedDeal = showingCheapsharkPrice;
        String cheapsharkDealUrl = showingCheapsharkPrice ? game.getCheapsharkDealUrl() : null;
        return new RankingResultDto(
                game.getIgdbGameId(),
                game.getTitle(),
                game.getIgdbRating(),
                game.getHltbHours(),
                game.isHltbFound(),
                priceCents,
                priceIsTrackedDeal,
                valueScore,
                game.getCoverImageUrl(),
                game.getIgdbUrl(),
                cheapsharkDealUrl,
                game.getSteamAppId(),
                platformIds,
                game.getAgeRatingDisplay()
        );
    }

    private static final BigDecimal PLAYTIME_CAP = BigDecimal.valueOf(200);
    private static final int FREE_NOMINAL_CENTS = 100;

    /** Returns the effective price, substituting $1.00 for free games when included. */
    private static Integer effectivePriceCents(GameCache game, boolean includeFree) {
        if (game.isFree() && includeFree) return FREE_NOMINAL_CENTS;
        return game.getEffectivePriceCents();
    }

    private BigDecimal computeValueScore(GameCache game, RankingQueryDto query) {
        Integer priceCents = effectivePriceCents(game, query.includeFreeToPlay());
        if (priceCents == null || priceCents <= 0) {
            throw new IllegalStateException("Rankable game missing usable price: " + game.getIgdbGameId());
        }

        double rating = game.getIgdbRating().doubleValue();
        double hours = game.getHltbHours().min(PLAYTIME_CAP).doubleValue();
        double price = priceCents / 100.0;

        double rW = query.effectiveRatingWeight().doubleValue();
        double pW = query.effectivePlaytimeWeight().doubleValue();
        double prW = query.effectivePriceWeight().doubleValue();

        double score = Math.pow(rating, rW) * Math.pow(hours, pW) / Math.pow(price, prW);
        return BigDecimal.valueOf(score).setScale(4, RoundingMode.HALF_UP);
    }
}
