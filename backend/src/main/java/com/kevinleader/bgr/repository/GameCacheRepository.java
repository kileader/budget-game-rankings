package com.kevinleader.bgr.repository;

import com.kevinleader.bgr.entity.GameCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GameCacheRepository extends JpaRepository<GameCache, Long> {

    List<GameCache> findBySteamAppIdIsNotNull();

    @Query("SELECT g FROM GameCache g WHERE g.lastHltbSync IS NULL")
    List<GameCache> findAllNeedingHltbSync();

    @Modifying
    @Transactional
    @Query("UPDATE GameCache g SET g.lastHltbSync = NULL")
    int clearAllHltbSync();

    @Query("""
            SELECT g
            FROM GameCache g
            WHERE g.igdbRating IS NOT NULL
              AND g.igdbRatingCount >= 10
              AND g.hltbHours IS NOT NULL
              AND g.isFree = false
              AND g.isMultiplayerOnly = false
              AND (g.cheapsharkPriceCents IS NOT NULL OR g.estimatedPriceCents IS NOT NULL)
            """)
    List<GameCache> findAllRankable();

    /** Broader query that includes free and multiplayer-only games (filtered in Java by query flags). */
    @Query("""
            SELECT g
            FROM GameCache g
            WHERE g.igdbRating IS NOT NULL
              AND g.igdbRatingCount >= 10
              AND g.hltbHours IS NOT NULL
              AND (g.isFree = true OR g.cheapsharkPriceCents IS NOT NULL OR g.estimatedPriceCents IS NOT NULL)
            """)
    List<GameCache> findAllScorable();

    // RANKABLE CRITERIA — keep in sync with findAllRankable() and findRankableGenres below.

    /** Distinct genre IDs present in rankable games, joined with genre_hltb_fallback for names. */
    @Query(value = """
            SELECT gh.igdb_genre_id AS id, gh.genre_name AS name
            FROM genre_hltb_fallback gh
            WHERE EXISTS (
                SELECT 1 FROM game_cache g
                WHERE g.igdb_rating IS NOT NULL
                  AND g.igdb_rating_count >= 10
                  AND g.hltb_hours IS NOT NULL
                  AND g.is_free = false
                  AND g.is_multiplayer_only = false
                  AND (g.cheapshark_price_cents IS NOT NULL OR g.estimated_price_cents IS NOT NULL)
                  AND gh.igdb_genre_id = ANY(g.genre_ids)
            )
            ORDER BY gh.genre_name
            """, nativeQuery = true)
    List<Object[]> findRankableGenres();
}
