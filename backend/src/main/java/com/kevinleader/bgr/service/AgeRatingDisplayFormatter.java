package com.kevinleader.bgr.service;

import com.kevinleader.bgr.dto.igdb.IgdbAgeRatingDto;

import java.util.List;

/**
 * Builds a short label for parents (prefers ESRB, then PEGI, then any other organization).
 */
public final class AgeRatingDisplayFormatter {

    private AgeRatingDisplayFormatter() {
    }

    public static String fromIgdbAgeRatings(List<IgdbAgeRatingDto> ageRatings) {
        if (ageRatings == null || ageRatings.isEmpty()) {
            return null;
        }
        String esrb = findForOrganization(ageRatings, "ESRB");
        if (esrb != null) {
            return esrb;
        }
        String pegi = findForOrganization(ageRatings, "PEGI");
        if (pegi != null) {
            return pegi;
        }
        for (IgdbAgeRatingDto ar : ageRatings) {
            String label = formatOne(ar);
            if (label != null) {
                return label;
            }
        }
        return null;
    }

    private static String findForOrganization(List<IgdbAgeRatingDto> ageRatings, String orgMatch) {
        for (IgdbAgeRatingDto ar : ageRatings) {
            if (ar.organization() == null || ar.organization().name() == null) {
                continue;
            }
            if (orgMatch.equalsIgnoreCase(ar.organization().name().trim())) {
                String label = formatOne(ar);
                if (label != null) {
                    return label;
                }
            }
        }
        return null;
    }

    private static String formatOne(IgdbAgeRatingDto ar) {
        String org = ar.organization() != null && ar.organization().name() != null
                ? ar.organization().name().trim()
                : null;
        String rating = ar.ratingCategory() != null && ar.ratingCategory().rating() != null
                ? ar.ratingCategory().rating().trim()
                : null;
        if (rating != null && !rating.isEmpty() && org != null && !org.isEmpty()) {
            return org + " · " + rating;
        }
        if (rating != null && !rating.isEmpty()) {
            return rating;
        }
        return null;
    }
}
