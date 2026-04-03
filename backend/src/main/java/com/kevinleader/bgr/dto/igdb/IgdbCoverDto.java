package com.kevinleader.bgr.dto.igdb;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IgdbCoverDto(
        @JsonProperty("image_id") String imageId
) {}