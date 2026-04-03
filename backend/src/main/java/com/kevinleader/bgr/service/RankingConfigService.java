package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.config.RankingConfigDto;
import com.kevinleader.bgr.dto.config.RankingConfigRequestDto;
import com.kevinleader.bgr.entity.AppUser;
import com.kevinleader.bgr.entity.RankingConfig;
import com.kevinleader.bgr.repository.RankingConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankingConfigService {

    private final RankingConfigRepository rankingConfigRepository;

    public RankingConfigService(RankingConfigRepository rankingConfigRepository) {
        this.rankingConfigRepository = rankingConfigRepository;
    }

    public List<RankingConfigDto> listConfigs(AppUser user) {
        return rankingConfigRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public RankingConfigDto getConfig(AppUser user, Long id) {
        RankingConfig config = rankingConfigRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Ranking config not found"));
        return toDto(config);
    }

    public RankingConfigDto createConfig(AppUser user, RankingConfigRequestDto request) {
        if (rankingConfigRepository.existsByUserAndName(user, request.name())) {
            throw new IllegalArgumentException("A config named '" + request.name() + "' already exists");
        }
        RankingConfig config = toEntity(request, user);
        return toDto(rankingConfigRepository.save(config));
    }

    public RankingConfigDto updateConfig(AppUser user, Long id, RankingConfigRequestDto request) {
        RankingConfig config = rankingConfigRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Ranking config not found"));

        if (!config.getName().equals(request.name())
                && rankingConfigRepository.existsByUserAndName(user, request.name())) {
            throw new IllegalArgumentException("A config named '" + request.name() + "' already exists");
        }

        config.setName(request.name());
        config.setPlatformIds(toIntArray(request.platformIds()));
        config.setGenreIds(toIntArray(request.genreIds()));
        config.setReleaseYearMin(request.releaseYearMin());
        config.setReleaseYearMax(request.releaseYearMax());
        config.setMinPriceCents(request.minPriceCents() != null ? request.minPriceCents() : 0);
        config.setMaxPriceCents(request.maxPriceCents());
        config.setMinPlaytimeHours(request.minPlaytimeHours());
        config.setMaxPlaytimeHours(request.maxPlaytimeHours());

        return toDto(rankingConfigRepository.save(config));
    }

    public void deleteConfig(AppUser user, Long id) {
        RankingConfig config = rankingConfigRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Ranking config not found"));
        rankingConfigRepository.delete(config);
    }

    private RankingConfig toEntity(RankingConfigRequestDto request, AppUser user) {
        RankingConfig config = new RankingConfig();
        config.setUser(user);
        config.setName(request.name());
        config.setPlatformIds(toIntArray(request.platformIds()));
        config.setGenreIds(toIntArray(request.genreIds()));
        config.setReleaseYearMin(request.releaseYearMin());
        config.setReleaseYearMax(request.releaseYearMax());
        config.setMinPriceCents(request.minPriceCents() != null ? request.minPriceCents() : 0);
        config.setMaxPriceCents(request.maxPriceCents());
        config.setMinPlaytimeHours(request.minPlaytimeHours());
        config.setMaxPlaytimeHours(request.maxPlaytimeHours());
        return config;
    }

    private RankingConfigDto toDto(RankingConfig config) {
        return new RankingConfigDto(
                config.getId(),
                config.getName(),
                toIntegerList(config.getPlatformIds()),
                toIntegerList(config.getGenreIds()),
                config.getReleaseYearMin(),
                config.getReleaseYearMax(),
                config.getMinPriceCents(),
                config.getMaxPriceCents(),
                config.getMinPlaytimeHours(),
                config.getMaxPlaytimeHours(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }

    private int[] toIntArray(List<Integer> list) {
        if (list == null) return new int[0];
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    private List<Integer> toIntegerList(int[] array) {
        if (array == null) return List.of();
        return Arrays.stream(array).boxed().collect(Collectors.toList());
    }
}
