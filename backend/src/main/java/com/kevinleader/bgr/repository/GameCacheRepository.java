package com.kevinleader.bgr.repository;

import com.kevinleader.bgr.entity.GameCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameCacheRepository extends JpaRepository<GameCache, Long> {

    List<GameCache> findBySteamAppIdIsNotNull();

    @Query("SELECT g FROM GameCache g WHERE g.lastHltbSync IS NULL")
    List<GameCache> findAllNeedingHltbSync();
}
