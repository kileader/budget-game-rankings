package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.config.RankingConfigDto;
import com.kevinleader.bgr.dto.config.RankingConfigRequestDto;
import com.kevinleader.bgr.entity.AppUser;
import com.kevinleader.bgr.entity.RankingConfig;
import com.kevinleader.bgr.repository.RankingConfigRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import com.kevinleader.bgr.exception.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RankingConfigServiceTest {

    private final RankingConfigRepository repository = mock(RankingConfigRepository.class);
    private final RankingConfigService service = new RankingConfigService(repository);

    private AppUser testUser() {
        AppUser user = new AppUser();
        user.setUsername("kevin");
        user.setEmail("kevin@example.com");
        user.setRole("USER");
        user.setActive(true);
        return user;
    }

    private RankingConfig savedConfig(AppUser user, String name) {
        RankingConfig config = new RankingConfig();
        config.setUser(user);
        config.setName(name);
        config.setPlatformIds(new int[0]);
        config.setGenreIds(new int[0]);
        return config;
    }

    @Test
    void listConfigsReturnsMappedDtos() {
        AppUser user = testUser();
        RankingConfig config = savedConfig(user, "Budget RPGs");
        when(repository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(config));

        List<RankingConfigDto> result = service.listConfigs(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Budget RPGs");
    }

    @Test
    void getConfigThrowsWhenNotFound() {
        AppUser user = testUser();
        when(repository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getConfig(user, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Ranking config not found");
    }

    @Test
    void createConfigSavesAndReturnsDto() {
        AppUser user = testUser();
        RankingConfigRequestDto request = new RankingConfigRequestDto(
                "Budget RPGs", null, null, null, null, null, 2000, null, null, null, null, null
        );
        when(repository.existsByUserAndName(user, "Budget RPGs")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RankingConfigDto result = service.createConfig(user, request);

        assertThat(result.name()).isEqualTo("Budget RPGs");
        assertThat(result.maxPriceCents()).isEqualTo(2000);
        verify(repository).save(any());
    }

    @Test
    void createConfigRejectsDuplicateName() {
        AppUser user = testUser();
        RankingConfigRequestDto request = new RankingConfigRequestDto(
                "Budget RPGs", null, null, null, null, null, null, null, null, null, null, null
        );
        when(repository.existsByUserAndName(user, "Budget RPGs")).thenReturn(true);

        assertThatThrownBy(() -> service.createConfig(user, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void deleteConfigRemovesEntity() {
        AppUser user = testUser();
        RankingConfig config = savedConfig(user, "Budget RPGs");
        when(repository.findByIdAndUser(1L, user)).thenReturn(Optional.of(config));

        service.deleteConfig(user, 1L);

        verify(repository).delete(config);
    }

    @Test
    void deleteConfigThrowsWhenNotFound() {
        AppUser user = testUser();
        when(repository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteConfig(user, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Ranking config not found");
    }
}
