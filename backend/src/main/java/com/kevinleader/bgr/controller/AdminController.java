package com.kevinleader.bgr.controller;

import com.kevinleader.bgr.dto.admin.ActiveUpdateRequestDto;
import com.kevinleader.bgr.dto.admin.AdminUserDto;
import com.kevinleader.bgr.dto.admin.RoleUpdateRequestDto;
import com.kevinleader.bgr.job.CacheRefreshJob;
import com.kevinleader.bgr.security.AppUserPrincipal;
import com.kevinleader.bgr.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final CacheRefreshJob cacheRefreshJob;
    private final AdminUserService adminUserService;

    public AdminController(CacheRefreshJob cacheRefreshJob, AdminUserService adminUserService) {
        this.cacheRefreshJob = cacheRefreshJob;
        this.adminUserService = adminUserService;
    }

    @PostMapping("/sync")
    public ResponseEntity<String> triggerSync() {
        if (cacheRefreshJob.isRunning()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Cache refresh already in progress");
        }
        cacheRefreshJob.runCacheRefresh();
        return ResponseEntity.ok("Cache refresh completed");
    }

    @GetMapping("/users")
    public List<AdminUserDto> listUsers() {
        return adminUserService.listUsers();
    }

    @GetMapping("/users/{id}")
    public AdminUserDto getUser(@PathVariable Long id) {
        return adminUserService.getUser(id);
    }

    @PatchMapping("/users/{id}/active")
    public AdminUserDto setActive(@AuthenticationPrincipal AppUserPrincipal principal,
                                  @PathVariable Long id,
                                  @RequestBody ActiveUpdateRequestDto request) {
        return adminUserService.setActive(principal.getUser().getId(), id, request.active());
    }

    @PatchMapping("/users/{id}/role")
    public AdminUserDto setRole(@AuthenticationPrincipal AppUserPrincipal principal,
                                @PathVariable Long id,
                                @Valid @RequestBody RoleUpdateRequestDto request) {
        return adminUserService.setRole(principal.getUser().getId(), id, request.role());
    }
}
