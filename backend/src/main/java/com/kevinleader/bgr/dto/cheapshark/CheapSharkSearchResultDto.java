package com.kevinleader.bgr.dto.cheapshark;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Single element returned by CheapShark's search-by-Steam-App-ID endpoint
 * ({@code GET /games?steamAppID=N}).  The response is a JSON <em>array</em>
 * of these objects — not the {@link CheapSharkGameDto} structure used by
 * the per-game lookup endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CheapSharkSearchResultDto(
        String gameID,
        String steamAppID,
        String cheapest,
        String external
) {}
