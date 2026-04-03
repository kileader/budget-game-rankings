package com.kevinleader.bgr.controller;

import com.kevinleader.bgr.dto.config.RankingConfigDto;
import com.kevinleader.bgr.dto.config.RankingConfigRequestDto;
import com.kevinleader.bgr.security.AppUserPrincipal;
import com.kevinleader.bgr.service.RankingConfigService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users/me/ranking-configs")
public class RankingConfigController {

    private final RankingConfigService rankingConfigService;

    public RankingConfigController(RankingConfigService rankingConfigService) {
        this.rankingConfigService = rankingConfigService;
    }

    @GetMapping
    public List<RankingConfigDto> list(@AuthenticationPrincipal AppUserPrincipal principal) {
        return rankingConfigService.listConfigs(principal.getUser());
    }

    @GetMapping("/{id}")
    public RankingConfigDto get(@AuthenticationPrincipal AppUserPrincipal principal,
                                @PathVariable Long id) {
        return rankingConfigService.getConfig(principal.getUser(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RankingConfigDto create(@AuthenticationPrincipal AppUserPrincipal principal,
                                   @Valid @RequestBody RankingConfigRequestDto request) {
        return rankingConfigService.createConfig(principal.getUser(), request);
    }

    @PutMapping("/{id}")
    public RankingConfigDto update(@AuthenticationPrincipal AppUserPrincipal principal,
                                   @PathVariable Long id,
                                   @Valid @RequestBody RankingConfigRequestDto request) {
        return rankingConfigService.updateConfig(principal.getUser(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AppUserPrincipal principal,
                       @PathVariable Long id) {
        rankingConfigService.deleteConfig(principal.getUser(), id);
    }
}
