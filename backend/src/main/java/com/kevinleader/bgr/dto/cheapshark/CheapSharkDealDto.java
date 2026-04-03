package com.kevinleader.bgr.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheapSharkDealDto(
        String storeID,
        String dealID,
        String price,
        String retailPrice
) {}
