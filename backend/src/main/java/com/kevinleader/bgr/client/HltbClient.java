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

import java.util.Arrays;
import java.util.List;

@Component
public class HltbClient {

    private static final Logger log = LoggerFactory.getLogger(HltbClient.class);

    private static final long TOKEN_TTL_MS = 60 * 60 * 1000L; // 1 hour

    private final RestClient hltbRestClient;
    private final long requestDelayMs;

    private HltbTokenResponse cachedInit;
    private long initFetchedAt;

    public HltbClient(@Qualifier("hltbRestClient") RestClient hltbRestClient,
                      @Value("${hltb.request-delay-ms}") long requestDelayMs) {
        this.hltbRestClient = hltbRestClient;
        this.requestDelayMs = requestDelayMs;
    }

    /**
     * Fetches the init payload (token + honeypot key/value) from /api/find/init.
     * Caches for 1 hour.
     */
    public synchronized HltbTokenResponse getInit() {
        long now = System.currentTimeMillis();
        if (cachedInit != null && (now - initFetchedAt) < TOKEN_TTL_MS) {
            return cachedInit;
        }

        try {
            HltbTokenResponse response = hltbRestClient.get()
                    .uri("/api/find/init?t={ts}", now)
                    .retrieve()
                    .body(HltbTokenResponse.class);

            if (response == null || response.token() == null || response.token().isBlank()) {
                log.warn("HLTB init response was empty or missing token field");
                return null;
            }

            cachedInit = response;
            initFetchedAt = now;
            log.debug("Fetched HLTB init: token={}, hpKey={}", response.token(), response.hpKey());
            return cachedInit;

        } catch (RestClientException e) {
            log.warn("Failed to fetch HLTB init: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Searches HLTB for a game by title via POST /api/find.
     * Sends the token and honeypot headers extracted from /api/find/init.
     * Applies a rate-limiting delay after each call.
     */
    public HltbSearchResponse search(String title) {
        HltbTokenResponse init = getInit();
        if (init == null) {
            log.warn("Skipping HLTB search for '{}': no valid init", title);
            return null;
        }

        List<String> terms = Arrays.asList(title.split("\\s+"));
        HltbSearchRequest body = new HltbSearchRequest(terms);
        body.setHoneypot(init.hpKey(), init.hpVal());

        try {
            RestClient.RequestBodySpec req = hltbRestClient.post()
                    .uri("/api/find")
                    .header("Content-Type", "application/json")
                    .header("x-auth-token", init.token());

            if (init.hpKey() != null && init.hpVal() != null) {
                req = req.header("x-hp-key", init.hpKey())
                         .header("x-hp-val", init.hpVal());
            }

            return req.body(body)
                    .retrieve()
                    .body(HltbSearchResponse.class);

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
