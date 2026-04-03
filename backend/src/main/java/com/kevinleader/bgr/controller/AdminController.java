package com.kevinleader.bgr.controller;

import com.kevinleader.bgr.job.CacheRefreshJob;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    // NOTE: runCacheRefresh() is synchronous. POST /admin/sync will block until
    // the full sync completes. Async execution is a Phase 6 concern.
    private final CacheRefreshJob cacheRefreshJob;

    public AdminController(CacheRefreshJob cacheRefreshJob) {
        this.cacheRefreshJob = cacheRefreshJob;
    }

    @PostMapping("/sync")
    public ResponseEntity<String> triggerSync() {
        if (cacheRefreshJob.isRunning()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cache refresh already in progress");
        }
        cacheRefreshJob.runCacheRefresh();
        return ResponseEntity.ok("Cache refresh completed");
    }
}
