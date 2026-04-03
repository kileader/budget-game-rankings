package com.kevinleader.bgr.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheapSharkGameDto(
        CheapSharkInfoDto info,
        List<CheapSharkDealDto> deals
) {}
