package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.metadata.MetadataItemDto;
import com.kevinleader.bgr.repository.GameCacheRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetadataService {

    private final GameCacheRepository gameCacheRepository;

    public MetadataService(GameCacheRepository gameCacheRepository) {
        this.gameCacheRepository = gameCacheRepository;
    }

    public List<MetadataItemDto> getPlatforms() {
        return gameCacheRepository.findRankablePlatforms().stream()
                .map(row -> new MetadataItemDto(
                        ((Number) row[0]).intValue(),
                        (String) row[1]))
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
