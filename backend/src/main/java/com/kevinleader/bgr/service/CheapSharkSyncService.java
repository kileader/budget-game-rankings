package com.kevinleader.bgr.service;

import com.kevinleader.bgr.client.CheapSharkClient;
import com.kevinleader.bgr.dto.cheapshark.CheapSharkDealDto;
import com.kevinleader.bgr.dto.cheapshark.CheapSharkGameDto;
import com.kevinleader.bgr.entity.GameCache;
import com.kevinleader.bgr.repository.GameCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class CheapSharkSyncService {

    private static final Logger log = LoggerFactory.getLogger(CheapSharkSyncService.class);

    private final CheapSharkClient cheapSharkClient;
    private final GameCacheRepository gameCacheRepository;

    public CheapSharkSyncService(CheapSharkClient cheapSharkClient,
                                  GameCacheRepository gameCacheRepository) {
        this.cheapSharkClient = cheapSharkClient;
        this.gameCacheRepository = gameCacheRepository;
    }

    public void syncAll() {
        List<GameCache> games = gameCacheRepository.findBySteamAppIdIsNotNull();
        log.info("Starting CheapShark price sync for {} games", games.size());

        int processed = 0;
        int updated = 0;
        int skipped = 0;

        for (GameCache game : games) {
            CheapSharkGameDto response = cheapSharkClient.fetchBySteamAppId(game.getSteamAppId());

            if (response == null) {
                skipped++;
            } else {
                PricedDeal best = findLowestPriceDeal(response.deals());
                if (best == null) {
                    skipped++;
                } else {
                    applyDeal(game, best.deal(), best.price());
                    gameCacheRepository.save(game);
                    updated++;
                }
            }

            processed++;
            if (processed % 100 == 0) {
                log.info("CheapShark sync progress: {}/{} processed, {} updated, {} skipped",
                        processed, games.size(), updated, skipped);
            }
        }

        log.info("CheapShark sync finished. Total: {}, updated: {}, skipped: {}",
                games.size(), updated, skipped);
    }

    private record PricedDeal(CheapSharkDealDto deal, double price) {}

    private PricedDeal findLowestPriceDeal(List<CheapSharkDealDto> deals) {
        CheapSharkDealDto best = null;
        double bestPrice = Double.MAX_VALUE;

        for (CheapSharkDealDto deal : deals) {
            if (deal.price() == null) continue;
            try {
                double price = Double.parseDouble(deal.price());
                if (price < bestPrice) {
                    bestPrice = price;
                    best = deal;
                }
            } catch (NumberFormatException e) {
                log.warn("Skipping deal with malformed price '{}' (dealID={})", deal.price(), deal.dealID());
            }
        }

        return best == null ? null : new PricedDeal(best, bestPrice);
    }

    private void applyDeal(GameCache game, CheapSharkDealDto deal, double price) {
        game.setCheapsharkPriceCents((int) Math.round(price * 100));
        game.setCheapsharkDealUrl("https://www.cheapshark.com/redirect?dealID=" + deal.dealID());
        game.setFree(price == 0.0);
        game.setLastPriceSync(OffsetDateTime.now());
    }
}
