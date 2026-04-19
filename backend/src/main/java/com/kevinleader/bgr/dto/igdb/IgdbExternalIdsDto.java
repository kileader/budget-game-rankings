package com.kevinleader.bgr.dto.igdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * IGDB {@code external} object on {@link IgdbGameDto} — direct storefront ids (see IGDB changelog:
 * {@code external.steam} on games). Prefer this over scanning {@code external_games} when present.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IgdbExternalIdsDto(
        /** Raw JSON value — number or string from IGDB. */
        @JsonProperty("steam") JsonNode steam
) {
    /** Parsed Steam application id, or null. */
    public Integer steamAppId() {
        if (steam == null || steam.isNull()) {
            return null;
        }
        if (steam.isNumber()) {
            return steam.intValue();
        }
        if (steam.isTextual()) {
            try {
                return Integer.parseInt(steam.asText().trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
