package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.igdb.IgdbAgeRatingCategoryDto;
import com.kevinleader.bgr.dto.igdb.IgdbAgeRatingDto;
import com.kevinleader.bgr.dto.igdb.IgdbAgeRatingOrganizationDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgeRatingDisplayFormatterTest {

    @Test
    void prefersEsrbWhenPresent() {
        String label = AgeRatingDisplayFormatter.fromIgdbAgeRatings(List.of(
                pegi("12"),
                esrb("Teen")
        ));
        assertThat(label).isEqualTo("ESRB · Teen");
    }

    @Test
    void fallsBackToPegi() {
        String label = AgeRatingDisplayFormatter.fromIgdbAgeRatings(List.of(
                pegi("7")
        ));
        assertThat(label).isEqualTo("PEGI · 7");
    }

    @Test
    void usesFirstOtherWhenNoEsrbOrPegi() {
        String label = AgeRatingDisplayFormatter.fromIgdbAgeRatings(List.of(
                new IgdbAgeRatingDto(
                        new IgdbAgeRatingCategoryDto("MA 15+"),
                        new IgdbAgeRatingOrganizationDto("ACB")
                )
        ));
        assertThat(label).isEqualTo("ACB · MA 15+");
    }

    @Test
    void returnsNullForEmpty() {
        assertThat(AgeRatingDisplayFormatter.fromIgdbAgeRatings(null)).isNull();
        assertThat(AgeRatingDisplayFormatter.fromIgdbAgeRatings(List.of())).isNull();
    }

    private static IgdbAgeRatingDto esrb(String rating) {
        return new IgdbAgeRatingDto(
                new IgdbAgeRatingCategoryDto(rating),
                new IgdbAgeRatingOrganizationDto("ESRB")
        );
    }

    private static IgdbAgeRatingDto pegi(String rating) {
        return new IgdbAgeRatingDto(
                new IgdbAgeRatingCategoryDto(rating),
                new IgdbAgeRatingOrganizationDto("PEGI")
        );
    }
}
