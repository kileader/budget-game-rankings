package com.kevinleader.bgr.dto.igdb;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IgdbAgeRatingDto(
        @JsonProperty("rating_category") IgdbAgeRatingCategoryDto ratingCategory,
        IgdbAgeRatingOrganizationDto organization
) {}
