package com.kevinleader.bgr.service;

import com.kevinleader.bgr.entity.GameCache;
import com.kevinleader.bgr.repository.GameCacheRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceEstimationServiceTest {

    @Mock
    private GameCacheRepository gameCacheRepository;

    @InjectMocks
    private PriceEstimationService priceEstimationService;

    @Test
    void includesPcTierWhenSteamPresentEvenIfPlatformsOmitWindows() {
        GameCache g = new GameCache();
        g.setIgdbGameId(1L);
        g.setPlatformIds(new int[]{508, 130});
        g.setSteamAppId(2379780);

        when(gameCacheRepository.findAll()).thenReturn(List.of(g));

        priceEstimationService.estimateAll();

        assertThat(g.getEstimatedPriceCents()).isEqualTo(1499);
        verify(gameCacheRepository).saveAll(anyList());
    }

    @Test
    void estimatesFromPcTierWhenPlatformsEmptyButSteamPresent() {
        GameCache g = new GameCache();
        g.setIgdbGameId(2L);
        g.setPlatformIds(new int[0]);
        g.setSteamAppId(123);

        when(gameCacheRepository.findAll()).thenReturn(List.of(g));

        priceEstimationService.estimateAll();

        assertThat(g.getEstimatedPriceCents()).isEqualTo(1499);
        verify(gameCacheRepository).saveAll(anyList());
    }

    @Test
    void skipsWhenNoPlatformsAndNoSteam() {
        GameCache g = new GameCache();
        g.setIgdbGameId(3L);
        g.setPlatformIds(new int[0]);
        g.setSteamAppId(null);

        when(gameCacheRepository.findAll()).thenReturn(List.of(g));

        priceEstimationService.estimateAll();

        assertThat(g.getEstimatedPriceCents()).isNull();
        verify(gameCacheRepository, never()).saveAll(anyList());
    }
}
