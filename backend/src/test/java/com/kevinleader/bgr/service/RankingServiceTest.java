package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.ranking.RankingPageDto;
import com.kevinleader.bgr.dto.ranking.RankingQueryDto;
import com.kevinleader.bgr.dto.ranking.RankingSort;
import com.kevinleader.bgr.dto.ranking.SortDirection;
import com.kevinleader.bgr.entity.GameCache;
import com.kevinleader.bgr.repository.GameCacheRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RankingServiceTest {

    @Test
    void ranksByValueScoreDescendingUsingEffectivePrice() {
        GameCache cheaperGame = buildGame(1L, "Cheaper Game", "90.00", "10.00",
                1000, null, LocalDate.of(2020, 1, 1), new int[]{130}, new int[]{12});
        GameCache pricierGame = buildGame(2L, "Pricier Game", "90.00", "10.00",
                2000, null, LocalDate.of(2021, 1, 1), new int[]{130}, new int[]{12});

        RankingService service = serviceWithGames(cheaperGame, pricierGame);

        RankingPageDto page = service.getRankingsPage(query(RankingSort.VALUE_SCORE));

        assertThat(page.total()).isEqualTo(2);
        assertThat(page.results()).extracting(result -> result.igdbGameId())
                .containsExactly(1L, 2L);
        assertThat(page.results().getFirst().valueScore())
                .isGreaterThan(page.results().get(1).valueScore());
    }

    @Test
    void filtersByPlatformGenrePricePlaytimeAndReleaseYear() {
        GameCache matching = buildGame(1L, "Matching", "85.00", "12.00",
                2500, null, LocalDate.of(2022, 5, 10), new int[]{130}, new int[]{12});
        GameCache wrongPlatform = buildGame(2L, "Wrong Platform", "85.00", "12.00",
                2500, null, LocalDate.of(2022, 5, 10), new int[]{167}, new int[]{12});
        GameCache wrongGenre = buildGame(3L, "Wrong Genre", "85.00", "12.00",
                2500, null, LocalDate.of(2022, 5, 10), new int[]{130}, new int[]{5});
        GameCache wrongPrice = buildGame(4L, "Wrong Price", "85.00", "12.00",
                5000, null, LocalDate.of(2022, 5, 10), new int[]{130}, new int[]{12});
        GameCache wrongHours = buildGame(5L, "Wrong Hours", "85.00", "4.00",
                2500, null, LocalDate.of(2022, 5, 10), new int[]{130}, new int[]{12});
        GameCache wrongYear = buildGame(6L, "Wrong Year", "85.00", "12.00",
                2500, null, LocalDate.of(2018, 5, 10), new int[]{130}, new int[]{12});

        RankingService service = serviceWithGames(
                matching, wrongPlatform, wrongGenre, wrongPrice, wrongHours, wrongYear
        );

        RankingPageDto page = service.getRankingsPage(new RankingQueryDto(
                List.of(130),
                List.of(12),
                2020,
                2024,
                1000,
                3000,
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                RankingSort.VALUE_SCORE,
                SortDirection.DESC,
                0,
                50
        ));

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.results()).extracting(result -> result.igdbGameId())
                .containsExactly(1L);
    }

    @Test
    void sortsByReleaseDateDescendingAndPaginates() {
        GameCache oldest = buildGame(1L, "Oldest", "80.00", "10.00",
                3000, null, LocalDate.of(2019, 1, 1), new int[]{130}, new int[]{12});
        GameCache newest = buildGame(2L, "Newest", "80.00", "10.00",
                3000, null, LocalDate.of(2024, 1, 1), new int[]{130}, new int[]{12});
        GameCache middle = buildGame(3L, "Middle", "80.00", "10.00",
                3000, null, LocalDate.of(2021, 1, 1), new int[]{130}, new int[]{12});

        RankingService service = serviceWithGames(oldest, newest, middle);

        RankingPageDto page = service.getRankingsPage(new RankingQueryDto(
                null, null, null, null, null, null, null, null,
                RankingSort.RELEASE_DATE, SortDirection.DESC,
                1,
                1
        ));

        assertThat(page.total()).isEqualTo(3);
        assertThat(page.offset()).isEqualTo(1);
        assertThat(page.limit()).isEqualTo(1);
        assertThat(page.results()).extracting(result -> result.igdbGameId())
                .containsExactly(3L);
    }

    @Test
    void rejectsInvalidFilterRanges() {
        RankingService service = serviceWithGames();

        assertThatThrownBy(() -> service.getRankingsPage(new RankingQueryDto(
                null, null, 2025, 2020, null, null, null, null,
                RankingSort.VALUE_SCORE, SortDirection.DESC, 0, 100
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("releaseYearMin");
    }

    private RankingService serviceWithGames(GameCache... games) {
        GameCacheRepository repository = mock(GameCacheRepository.class);
        when(repository.findAllRankable()).thenReturn(List.of(games));
        return new RankingService(repository);
    }

    private RankingQueryDto query(RankingSort sort) {
        return new RankingQueryDto(
                null, null, null, null, null, null, null, null,
                sort, SortDirection.DESC, 0, 100
        );
    }

    private GameCache buildGame(Long id,
                                String title,
                                String rating,
                                String hours,
                                Integer cheapsharkPriceCents,
                                Integer estimatedPriceCents,
                                LocalDate releaseDate,
                                int[] platformIds,
                                int[] genreIds) {
        GameCache game = new GameCache();
        game.setIgdbGameId(id);
        game.setTitle(title);
        game.setIgdbRating(new BigDecimal(rating));
        game.setIgdbRatingCount(100);
        game.setHltbHours(new BigDecimal(hours));
        game.setCheapsharkPriceCents(cheapsharkPriceCents);
        game.setEstimatedPriceCents(estimatedPriceCents);
        game.setFirstReleaseDate(releaseDate);
        game.setPlatformIds(platformIds);
        game.setGenreIds(genreIds);
        game.setFree(false);
        game.setMultiplayerOnly(false);
        return game;
    }
}
