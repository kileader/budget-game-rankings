package com.kevinleader.bgr.repository;

import com.kevinleader.bgr.entity.GameCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameCacheRepository extends JpaRepository<GameCache, Long> {
}
