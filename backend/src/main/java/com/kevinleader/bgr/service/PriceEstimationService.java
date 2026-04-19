package com.kevinleader.bgr.service;

import com.kevinleader.bgr.entity.GameCache;
import com.kevinleader.bgr.repository.GameCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PriceEstimationService {

    private static final Logger log = LoggerFactory.getLogger(PriceEstimationService.class);

    private static final int BATCH_SIZE = 500;

    /**
     * Maps IGDB platform IDs to rough tier prices in cents (fallback when CheapShark has no deal).
     * PC / Mac / Linux use a modest digital baseline so multi-platform rows are not dominated
     * by console MSRP (e.g. indie on Steam + PS5 was showing $69.99).
     */
    private static final Map<Integer, Integer> PLATFORM_PRICE_MAP = Map.ofEntries(
            Map.entry(6, 1499),    // PC (Windows) — typical indie digital list; CheapShark overrides when present
            Map.entry(14, 1499),   // Mac
            Map.entry(3, 1499),    // Linux

            // Current gen -- $69.99 baseline
            Map.entry(508, 6999),  // Nintendo Switch 2
            Map.entry(167, 6999),  // PlayStation 5
            Map.entry(169, 6999),  // Xbox Series X|S

            // Prior current gen / late lifecycle -- $59.99 baseline
            Map.entry(130, 5999),  // Nintendo Switch

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
     * Assigns estimated prices to games that have at least one IGDB platform id <strong>or</strong> a
     * non-null {@code steam_app_id} (Steam-only rows use the PC tier alone).
     * Uses the lowest matched tier — when CheapShark is missing, this approximates
     * "you can buy it on the cheapest listed platform family" instead of max console MSRP.
     * <p>
     * If IGDB lists a Steam {@code steam_app_id} but omits Windows (6) from {@code platform_ids},
     * we still include the PC tier so console MSRP (e.g. $69.99) does not dominate indies sold on Steam.
     */
    public void estimateAll() {
        List<GameCache> all = gameCacheRepository.findAll();

        List<GameCache> toSave = new ArrayList<>();
        int estimated = 0;
        int skipped = 0;

        for (GameCache game : all) {
            int[] platformIds = game.getPlatformIds();
            boolean hasPlatforms = platformIds != null && platformIds.length > 0;
            Integer steamAppId = game.getSteamAppId();

            if (!hasPlatforms && steamAppId == null) {
                skipped++;
                continue;
            }

            Set<Integer> idsForTier = new LinkedHashSet<>();
            if (platformIds != null && platformIds.length > 0) {
                for (int p : platformIds) {
                    idsForTier.add(p);
                }
            }
            if (steamAppId != null) {
                idsForTier.add(6);
            }

            if (idsForTier.isEmpty()) {
                skipped++;
                continue;
            }

            Integer bestEstimate = null;
            for (int platformId : idsForTier) {
                Integer price = PLATFORM_PRICE_MAP.get(platformId);
                if (price != null) {
                    if (bestEstimate == null || price < bestEstimate) {
                        bestEstimate = price;
                    }
                }
            }

            if (bestEstimate != null) {
                game.setEstimatedPriceCents(bestEstimate);
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
