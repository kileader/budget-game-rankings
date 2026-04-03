package com.kevinleader.bgr.client;

import com.kevinleader.bgr.dto.cheapshark.CheapSharkGameDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CheapSharkClient {

    private static final Logger log = LoggerFactory.getLogger(CheapSharkClient.class);

    private final RestClient cheapSharkRestClient;

    public CheapSharkClient(@Qualifier("cheapSharkRestClient") RestClient cheapSharkRestClient) {
        this.cheapSharkRestClient = cheapSharkRestClient;
    }

    /**
     * Fetches deal data for a single game by Steam App ID.
     *
     * @param steamAppId the Steam application ID
     * @return a {@link CheapSharkGameDto} with deal information, or {@code null} if the game
     *         is not listed on CheapShark or the response contains no deals
     */
    public CheapSharkGameDto fetchBySteamAppId(int steamAppId) {
        try {
            CheapSharkGameDto response = cheapSharkRestClient.get()
                    .uri("/games?steamAppID={id}", steamAppId)
                    .retrieve()
                    .body(CheapSharkGameDto.class);

            if (response == null || response.deals() == null || response.deals().isEmpty()) {
                return null;
            }
            return response;
        } catch (RestClientException e) {
            log.warn("CheapShark request failed for steamAppId={}: {}", steamAppId, e.getMessage());
            return null;
        }
    }
}
