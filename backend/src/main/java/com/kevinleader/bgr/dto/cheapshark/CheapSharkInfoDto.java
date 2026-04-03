package com.kevinleader.bgr.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheapSharkInfoDto(
        String title,
        String steamAppID
) {}
