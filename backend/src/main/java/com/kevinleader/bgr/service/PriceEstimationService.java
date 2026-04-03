package com.kevinleader.bgr.service;

import com.kevinleader.bgr.entity.GameCache;
import com.kevinleader.bgr.repository.GameCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PriceEstimationService {

    private static final Logger log = LoggerFactory.getLogger(PriceEstimationService.class);

    private static final int BATCH_SIZE = 500;

    /**
     * Maps IGDB platform IDs to estimated retail price in cents.
     * PC platforms (6, 14, 3) are intentionally absent -- CheapShark handles those.
     */
    private static final Map<Integer, Integer> PLATFORM_PRICE_MAP = Map.ofEntries(
            // Current gen -- $59.99
            Map.entry(130, 5999),  // Nintendo Switch
            Map.entry(167, 5999),  // PlayStation 5
            Map.entry(169, 5999),  // Xbox Series X|S

            // Previous gen -- $39.99
            Map.entry(48, 3999),   // PlayStation 4
            Map.entry(49, 3999),   // Xbox One
            Map.entry(8,  3999),   // Nintendo 3DS
            Map.entry(41, 3999),   // Wii U

            // Older gen -- $19.99
            Map.entry(9,  1999),   // PlayStation 3
            Map.entry(12, 1999),   // Xbox 360
            Map.entry(18, 1999),   // Nintendo GameCube
            Map.entry(19, 1999),   // Nintendo 64

            // Retro -- $9.99
            Map.entry(11, 999),    // Xbox (original)
            Map.entry(21, 999),    // Nintendo Game Boy Advance
            Map.entry(22, 999),    // Nintendo Game Boy Color
            Map.entry(33, 999),    // Nintendo Game Boy
            Map.entry(24, 999)     // Nintendo NES/Famicom
    );

    private final GameCacheRepository gameCacheRepository;

    public PriceEstimationService(GameCacheRepository gameCacheRepository) {
        this.gameCacheRepository = gameCacheRepository;
    }

    /**
     * Assigns estimated prices to all GameCache records that have platform IDs.
     * Uses the highest-tier price found across a game's platforms.
     * Saves in batches of 500. PC-only games are skipped.
     */
    public void estimateAll() {
        List<GameCache> all = gameCacheRepository.findAll();

        List<GameCache> toSave = new ArrayList<>();
        int estimated = 0;
        int skipped = 0;

        for (GameCache game : all) {
            int[] platformIds = game.getPlatformIds();
            if (platformIds == null || platformIds.length == 0) {
                skipped++;
                continue;
            }

            Integer highestPrice = null;
            for (int platformId : platformIds) {
                Integer price = PLATFORM_PRICE_MAP.get(platformId);
                if (price != null) {
                    if (highestPrice == null || price > highestPrice) {
                        highestPrice = price;
                    }
                }
            }

            if (highestPrice != null) {
                game.setEstimatedPriceCents(highestPrice);
                game.setLastPriceSync(OffsetDateTime.now());
                toSave.add(game);
                estimated++;
            } else {
                skipped++;
            }
        }

        // Save in batches
        for (int i = 0; i < toSave.size(); i += BATCH_SIZE) {
            List<GameCache> batch = toSave.subList(i, Math.min(i + BATCH_SIZE, toSave.size()));
            gameCacheRepository.saveAll(batch);
            log.debug("Saved price estimation batch {}/{}", i / BATCH_SIZE + 1,
                    (int) Math.ceil((double) toSave.size() / BATCH_SIZE));
        }

        log.info("Price estimation complete -- estimated: {}, skipped (no applicable platform): {}",
                estimated, skipped);
    }
}
