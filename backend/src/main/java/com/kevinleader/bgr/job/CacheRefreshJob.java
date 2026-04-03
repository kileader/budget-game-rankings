package com.kevinleader.bgr.job;

import com.kevinleader.bgr.service.CheapSharkSyncService;
import com.kevinleader.bgr.service.HltbSyncService;
import com.kevinleader.bgr.service.IgdbSyncService;
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
    private final CheapSharkSyncService cheapSharkSyncService;
    private final HltbSyncService hltbSyncService;

    public CacheRefreshJob(IgdbSyncService igdbSyncService,
                           CheapSharkSyncService cheapSharkSyncService,
                           HltbSyncService hltbSyncService) {
        this.igdbSyncService = igdbSyncService;
        this.cheapSharkSyncService = cheapSharkSyncService;
        this.hltbSyncService = hltbSyncService;
    }

    public boolean isRunning() {
        return running.get();
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
            log.info("Phase 1/3: IGDB sync");
            try {
                igdbSyncService.syncAll();
            } catch (Exception e) {
                log.error("IGDB sync failed -- continuing to next phase", e);
            }

            log.info("Phase 2/3: CheapShark sync");
            try {
                cheapSharkSyncService.syncAll();
            } catch (Exception e) {
                log.error("CheapShark sync failed -- continuing to next phase", e);
            }

            log.info("Phase 3/3: HLTB sync");
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
