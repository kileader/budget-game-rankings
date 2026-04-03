package com.kevinleader.bgr.client;

import com.kevinleader.bgr.dto.hltb.HltbSearchRequest;
import com.kevinleader.bgr.dto.hltb.HltbSearchResponse;
import com.kevinleader.bgr.dto.hltb.HltbTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Component
public class HltbClient {

    private static final Logger log = LoggerFactory.getLogger(HltbClient.class);

    private static final long TOKEN_TTL_MS = 60 * 60 * 1000L; // 1 hour

    private final RestClient hltbRestClient;
    private final long requestDelayMs;

    private String cachedToken;
    private long tokenFetchedAt;

    public HltbClient(@Qualifier("hltbRestClient") RestClient hltbRestClient,
                      @Value("${hltb.request-delay-ms}") long requestDelayMs) {
        this.hltbRestClient = hltbRestClient;
        this.requestDelayMs = requestDelayMs;
    }

    /**
     * Returns a valid session token, fetching a new one if the cached token has expired.
     * Synchronized to prevent concurrent token refreshes.
     */
    public synchronized String getToken() {
        long now = System.currentTimeMillis();
        if (cachedToken != null && (now - tokenFetchedAt) < TOKEN_TTL_MS) {
            return cachedToken;
        }

        try {
            HltbTokenResponse response = hltbRestClient.get()
                    .uri("/api/finder/init?t={ts}", now)
                    .retrieve()
                    .body(HltbTokenResponse.class);

            if (response == null || response.token() == null || response.token().isBlank()) {
                log.warn("HLTB token response was empty or missing token field");
                return null;
            }

            cachedToken = response.token();
            tokenFetchedAt = now;
            log.debug("Fetched new HLTB token");
            return cachedToken;

        } catch (RestClientException e) {
            log.warn("Failed to fetch HLTB token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Searches HLTB for a game by title.
     * Splits the title on whitespace to build the searchTerms array.
     * Applies a rate-limiting delay after each call.
     *
     * @param title the game title to search for
     * @return the search response, or {@code null} if the token is unavailable or the request fails
     */
    public HltbSearchResponse search(String title) {
        String token = getToken();
        if (token == null) {
            log.warn("Skipping HLTB search for '{}': no valid token", title);
            return null;
        }

        List<String> terms = Arrays.asList(title.split("\\s+"));
        HltbSearchRequest body = new HltbSearchRequest(terms);

        try {
            HltbSearchResponse response = hltbRestClient.post()
                    .uri("/api/finder")
                    .header("x-auth-token", token)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(HltbSearchResponse.class);

            return response;

        } catch (RestClientException e) {
            log.warn("HLTB search failed for title='{}': {}", title, e.getMessage());
            return null;

        } finally {
            if (requestDelayMs > 0) {
                try {
                    Thread.sleep(requestDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
