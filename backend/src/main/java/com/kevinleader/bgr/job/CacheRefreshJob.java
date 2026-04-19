package com.kevinleader.bgr.job;

import com.kevinleader.bgr.service.CheapSharkSyncService;
import com.kevinleader.bgr.service.HltbSyncService;
import com.kevinleader.bgr.service.IgdbSyncService;
import com.kevinleader.bgr.service.PriceEstimationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class CacheRefreshJob {

    private static final Logger log = LoggerFactory.getLogger(CacheRefreshJob.class);

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final IgdbSyncService igdbSyncService;
    private final PriceEstimationService priceEstimationService;
    private final CheapSharkSyncService cheapSharkSyncService;
    private final HltbSyncService hltbSyncService;

    public CacheRefreshJob(IgdbSyncService igdbSyncService,
                           PriceEstimationService priceEstimationService,
                           CheapSharkSyncService cheapSharkSyncService,
                           HltbSyncService hltbSyncService) {
        this.igdbSyncService = igdbSyncService;
        this.priceEstimationService = priceEstimationService;
        this.cheapSharkSyncService = cheapSharkSyncService;
        this.hltbSyncService = hltbSyncService;
    }

    public boolean isRunning() {
        return running.get();
    }

    /**
     * IGDB catalog sync only. Returns false if another cache job is already running.
     */
    public boolean runIgdbSyncOnly() {
        if (!running.compareAndSet(false, true)) {
            log.warn("IGDB-only sync skipped -- another cache job is in progress");
            return false;
        }
        long t0 = System.currentTimeMillis();
        try {
            log.info("IGDB-only sync started at {}", java.time.Instant.now());
            igdbSyncService.syncAll();
            log.info("IGDB-only sync finished. Elapsed: {} ms", System.currentTimeMillis() - t0);
            return true;
        } finally {
            running.set(false);
        }
    }

    /**
     * Tier price estimation only. Returns false if another cache job is already running.
     */
    public boolean runPriceEstimationOnly() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Price-estimation-only sync skipped -- another cache job is in progress");
            return false;
        }
        long t0 = System.currentTimeMillis();
        try {
            log.info("Price-estimation-only sync started at {}", java.time.Instant.now());
            priceEstimationService.estimateAll();
            log.info("Price-estimation-only sync finished. Elapsed: {} ms", System.currentTimeMillis() - t0);
            return true;
        } finally {
            running.set(false);
        }
    }

    /**
     * CheapShark storefront prices only. Returns false if another cache job is already running.
     */
    public boolean runCheapSharkSyncOnly() {
        if (!running.compareAndSet(false, true)) {
            log.warn("CheapShark-only sync skipped -- another cache job is in progress");
            return false;
        }
        long t0 = System.currentTimeMillis();
        try {
            log.info("CheapShark-only sync started at {}", java.time.Instant.now());
            cheapSharkSyncService.syncAll();
            log.info("CheapShark-only sync finished. Elapsed: {} ms", System.currentTimeMillis() - t0);
            return true;
        } finally {
            running.set(false);
        }
    }

    /**
     * Incremental HLTB hours sync only (games that still need sync). Returns false if another job is running.
     */
    public boolean runHltbSyncOnly() {
        if (!running.compareAndSet(false, true)) {
            log.warn("HLTB-only sync skipped -- another cache job is in progress");
            return false;
        }
        long t0 = System.currentTimeMillis();
        try {
            log.info("HLTB-only sync started at {}", java.time.Instant.now());
            hltbSyncService.syncAll();
            log.info("HLTB-only sync finished. Elapsed: {} ms", System.currentTimeMillis() - t0);
            return true;
        } finally {
            running.set(false);
        }
    }

    /**
     * Clears HLTB sync timestamps then runs a full HLTB pass. Returns {@code null} if another job is running.
     */
    public Integer runHltbResetAndSync() {
        if (!running.compareAndSet(false, true)) {
            log.warn("HLTB reset+sync skipped -- another cache job is in progress");
            return null;
        }
        long t0 = System.currentTimeMillis();
        try {
            log.info("HLTB reset+sync started at {}", java.time.Instant.now());
            int cleared = hltbSyncService.resetAll();
            hltbSyncService.syncAll();
            log.info("HLTB reset+sync finished (cleared {} rows). Elapsed: {} ms",
                    cleared, System.currentTimeMillis() - t0);
            return cleared;
        } finally {
            running.set(false);
        }
    }

    @Scheduled(cron = "${cache.refresh.cron}")
    public void runCacheRefresh() {
        if (!running.compareAndSet(false, true)) {
            log.warn("Cache refresh already in progress -- skipping this trigger");
            return;
        }
        long jobStart = System.currentTimeMillis();
        log.info("Cache refresh job started at {}", java.time.Instant.now());
        try {
            log.info("Step 1/4: IGDB sync");
            try {
                igdbSyncService.syncAll();
            } catch (Exception e) {
                log.error("IGDB sync failed -- continuing to next phase", e);
            }

            log.info("Step 2/4: price estimation sync");
            try {
                priceEstimationService.estimateAll();
            } catch (Exception e) {
                log.error("Price estimation sync failed -- continuing to next phase", e);
            }

            log.info("Step 3/4: CheapShark sync");
            try {
                cheapSharkSyncService.syncAll();
            } catch (Exception e) {
                log.error("CheapShark sync failed -- continuing to next phase", e);
            }

            log.info("Step 4/4: HLTB sync");
            try {
                hltbSyncService.syncAll();
            } catch (Exception e) {
                log.error("HLTB sync failed", e);
            }

            long elapsedMs = System.currentTimeMillis() - jobStart;
            log.info("Cache refresh job finished. Elapsed: {} ms ({} s)", elapsedMs, elapsedMs / 1000);
        } finally {
            running.set(false);
        }
    }
}
