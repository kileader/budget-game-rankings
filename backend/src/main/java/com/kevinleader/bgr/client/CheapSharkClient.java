package com.kevinleader.bgr.client;

import com.kevinleader.bgr.dto.cheapshark.CheapSharkGameDto;
import com.kevinleader.bgr.dto.cheapshark.CheapSharkSearchResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class CheapSharkClient {

    private static final Logger log = LoggerFactory.getLogger(CheapSharkClient.class);

    private final RestClient cheapSharkRestClient;

    public CheapSharkClient(@Qualifier("cheapSharkRestClient") RestClient cheapSharkRestClient) {
        this.cheapSharkRestClient = cheapSharkRestClient;
    }

    /**
     * Fetches deal data for a single game by Steam App ID.
     * <p>
     * This is a two-step process because the CheapShark API exposes two
     * different endpoints with different response shapes:
     * <ol>
     *   <li>{@code GET /games?steamAppID=N} — returns a JSON <em>array</em>
     *       of {@link CheapSharkSearchResultDto} (search results).</li>
     *   <li>{@code GET /games?id=N} — returns a single
     *       {@link CheapSharkGameDto} with {@code info} and {@code deals}.</li>
     * </ol>
     * We search first to resolve the CheapShark game ID, then fetch the
     * full deal list for that game.
     *
     * @param steamAppId the Steam application ID
     * @return a {@link CheapSharkGameDto} with deal information, or {@code null}
     *         if the game is not listed on CheapShark or has no deals
     */
    public CheapSharkGameDto fetchBySteamAppId(int steamAppId) {
        String cheapSharkGameId = searchGameIdBySteamAppId(steamAppId);
        if (cheapSharkGameId == null) {
            return null;
        }
        return fetchByGameId(cheapSharkGameId);
    }

    /**
     * Step 1: search by Steam App ID to get the CheapShark game ID.
     */
    private String searchGameIdBySteamAppId(int steamAppId) {
        try {
            List<CheapSharkSearchResultDto> results = cheapSharkRestClient.get()
                    .uri("/games?steamAppID={id}", steamAppId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (results == null || results.isEmpty()) {
                return null;
            }
            return results.getFirst().gameID();
        } catch (RestClientException e) {
            log.warn("CheapShark search failed for steamAppId={}: {}", steamAppId, e.getMessage());
            return null;
        }
    }

    /**
     * Step 2: fetch full game details (info + deals) by CheapShark game ID.
     */
    private CheapSharkGameDto fetchByGameId(String gameId) {
        try {
            CheapSharkGameDto response = cheapSharkRestClient.get()
                    .uri("/games?id={id}", gameId)
                    .retrieve()
                    .body(CheapSharkGameDto.class);

            if (response == null || response.deals() == null || response.deals().isEmpty()) {
                return null;
            }
            return response;
        } catch (RestClientException e) {
            log.warn("CheapShark game fetch failed for gameId={}: {}", gameId, e.getMessage());
            return null;
        }
    }
}
