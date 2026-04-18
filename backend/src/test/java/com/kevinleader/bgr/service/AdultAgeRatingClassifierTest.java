package com.kevinleader.bgr.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdultAgeRatingClassifierTest {

    @Test
    void nullOrBlankIsNotAdult() {
        assertThat(AdultAgeRatingClassifier.isAdultRated(null)).isFalse();
        assertThat(AdultAgeRatingClassifier.isAdultRated("")).isFalse();
        assertThat(AdultAgeRatingClassifier.isAdultRated("  ")).isFalse();
    }

    @Test
    void esrbMatureAndAo() {
        assertThat(AdultAgeRatingClassifier.isAdultRated("ESRB · Mature")).isTrue();
        assertThat(AdultAgeRatingClassifier.isAdultRated("ESRB · Adults Only")).isTrue();
        assertThat(AdultAgeRatingClassifier.isAdultRated("ESRB · AO")).isTrue();
        assertThat(AdultAgeRatingClassifier.isAdultRated("ESRB · M")).isTrue();
    }

    @Test
    void esrbNonAdult() {
        assertThat(AdultAgeRatingClassifier.isAdultRated("ESRB · Teen")).isFalse();
        assertThat(AdultAgeRatingClassifier.isAdultRated("ESRB · Everyone")).isFalse();
        assertThat(AdultAgeRatingClassifier.isAdultRated("ESRB · E10+")).isFalse();
    }

    @Test
    void pegi18() {
        assertThat(AdultAgeRatingClassifier.isAdultRated("PEGI · 18")).isTrue();
        assertThat(AdultAgeRatingClassifier.isAdultRated("PEGI · 12")).isFalse();
    }
}
