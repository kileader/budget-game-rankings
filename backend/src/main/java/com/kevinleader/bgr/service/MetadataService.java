package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.metadata.MetadataItemDto;
import com.kevinleader.bgr.repository.GameCacheRepository;
import com.kevinleader.bgr.repository.PlatformRefRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetadataService {

    private final PlatformRefRepository platformRefRepository;
    private final GameCacheRepository gameCacheRepository;

    public MetadataService(PlatformRefRepository platformRefRepository, GameCacheRepository gameCacheRepository) {
        this.platformRefRepository = platformRefRepository;
        this.gameCacheRepository = gameCacheRepository;
    }

    /** All platforms from reference table (picker), not only those on rankable cached games. */
    public List<MetadataItemDto> getPlatforms() {
        return platformRefRepository.findAllByOrderBySortOrderAsc().stream()
                .map(p -> new MetadataItemDto(p.getIgdbPlatformId(), p.getName()))
                .toList();
    }

    public List<MetadataItemDto> getGenres() {
        return gameCacheRepository.findRankableGenres().stream()
                .map(row -> new MetadataItemDto(
                        ((Number) row[0]).intValue(),
                        (String) row[1]))
                .toList();
    }
}
