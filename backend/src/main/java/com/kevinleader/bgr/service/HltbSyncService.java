package com.kevinleader.bgr.service;

import com.kevinleader.bgr.client.HltbClient;
import com.kevinleader.bgr.dto.hltb.HltbGameResult;
import com.kevinleader.bgr.dto.hltb.HltbSearchResponse;
import com.kevinleader.bgr.entity.GameCache;
import com.kevinleader.bgr.entity.GenreHltbFallback;
import com.kevinleader.bgr.repository.GameCacheRepository;
import com.kevinleader.bgr.repository.GenreHltbFallbackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HltbSyncService {

    private static final Logger log = LoggerFactory.getLogger(HltbSyncService.class);


    private final HltbClient hltbClient;
    private final GameCacheRepository gameCacheRepository;
    private final GenreHltbFallbackRepository genreHltbFallbackRepository;

    public HltbSyncService(HltbClient hltbClient,
                           GameCacheRepository gameCacheRepository,
                           GenreHltbFallbackRepository genreHltbFallbackRepository) {
        this.hltbClient = hltbClient;
        this.gameCacheRepository = gameCacheRepository;
        this.genreHltbFallbackRepository = genreHltbFallbackRepository;
    }

    /**
     * Clears lastHltbSync for all games so the next syncAll() re-fetches everything.
     */
    public int resetAll() {
        return gameCacheRepository.clearAllHltbSync();
    }

    public void syncAll() {
        List<GameCache> games = gameCacheRepository.findAllNeedingHltbSync();
        log.info("Starting HLTB sync for {} games", games.size());

        int processed = 0;
        int matched = 0;
        int fallback = 0;

        for (GameCache game : games) {
            HltbSearchResponse response = hltbClient.search(game.getTitle());

            if (response == null || response.data() == null || response.data().isEmpty()) {
                applyGenreFallback(game);
                fallback++;
            } else {
                HltbGameResult result = response.data().get(0);
                if (isTitleMatch(game.getTitle(), result.gameName())) {
                    applyHltbResult(game, result);
                    matched++;
                } else {
                    log.debug("Title mismatch: game='{}' hltb='{}' -- using fallback",
                            game.getTitle(), result.gameName());
                    applyGenreFallback(game);
                    fallback++;
                }
            }

            game.setLastHltbSync(OffsetDateTime.now());
            gameCacheRepository.save(game);

            processed++;
            if (processed % 50 == 0) {
                log.info("HLTB sync progress: {}/{} processed, {} matched, {} fallback",
                        processed, games.size(), matched, fallback);
            }
        }

        log.info("HLTB sync finished. Total: {}, matched: {}, fallback: {}",
                games.size(), matched, fallback);
    }

    // ── Title matching ────────────────────────────────────────────────────────

    private boolean isTitleMatch(String gameTitle, String hltbTitle) {
        if (gameTitle == null || hltbTitle == null) return false;
        String a = normalize(gameTitle);
        String b = normalize(hltbTitle);
        if (a.isEmpty() || b.isEmpty()) return false;
        String shorter = a.length() <= b.length() ? a : b;
        String longer  = a.length() <= b.length() ? b : a;
        if (shorter.length() >= 5 && longer.contains(shorter)) return true;
        return levenshtein(a, b) <= 3;
    }

    private String normalize(String title) {
        return title.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    /**
     * Iterative Levenshtein distance. O(m*n) time, O(n) space.
     */
    private int levenshtein(String a, String b) {
        int m = a.length();
        int n = b.length();
        int[] prev = new int[n + 1];
        int[] curr = new int[n + 1];

        for (int j = 0; j <= n; j++) prev[j] = j;

        for (int i = 1; i <= m; i++) {
            curr[0] = i;
            for (int j = 1; j <= n; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    curr[j] = prev[j - 1];
                } else {
                    curr[j] = 1 + Math.min(prev[j - 1], Math.min(prev[j], curr[j - 1]));
                }
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }

        return prev[n];
    }

    // ── Result application ────────────────────────────────────────────────────

    private void applyHltbResult(GameCache game, HltbGameResult result) {
        Integer seconds = result.compMain() != null && result.compMain() > 0
                ? result.compMain()
                : result.compPlus();

        if (seconds == null || seconds == 0) {
            applyGenreFallback(game);
            return;
        }

        BigDecimal hours = BigDecimal.valueOf(seconds)
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);

        game.setHltbHours(hours);
        game.setHltbFound(true);
    }

    private void applyGenreFallback(GameCache game) {
        game.setHltbFound(false);

        int[] genreIds = game.getGenreIds();
        if (genreIds == null || genreIds.length == 0) {
            return;
        }

        int firstGenreId = genreIds[0];
        Optional<GenreHltbFallback> fallbackOpt = genreHltbFallbackRepository.findById(firstGenreId);
        if (fallbackOpt.isPresent()) {
            game.setHltbHours(fallbackOpt.get().getAvgHours());
            log.debug("Applied genre fallback for game='{}', genre='{}', hours={}",
                    game.getTitle(), fallbackOpt.get().getGenreName(), fallbackOpt.get().getAvgHours());
        } else {
            log.debug("No fallback entry found for IGDB genre ID {}", firstGenreId);
        }
    }
}
