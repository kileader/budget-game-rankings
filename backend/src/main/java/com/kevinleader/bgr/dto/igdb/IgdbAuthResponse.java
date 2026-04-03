package com.kevinleader.bgr.dto.igdb;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IgdbAuthResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") long expiresIn
) {}