package com.kevinleader.bgr.service;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Detects mature / adult content ratings from {@link com.kevinleader.bgr.entity.GameCache#getAgeRatingDisplay()}.
 * Uses substring heuristics on IGDB-derived labels (e.g. "ESRB · Mature", "PEGI · 18").
 * Games with no label are treated as not adult for filtering (unknown stays visible).
 */
public final class AdultAgeRatingClassifier {

    private static final Pattern WORD_AO = Pattern.compile("\\bAO\\b", Pattern.CASE_INSENSITIVE);

    private AdultAgeRatingClassifier() {
    }

    public static boolean isAdultRated(String ageRatingDisplay) {
        if (ageRatingDisplay == null || ageRatingDisplay.isBlank()) {
            return false;
        }
        String u = ageRatingDisplay.toUpperCase(Locale.ROOT);

        if (u.contains("ADULTS ONLY")) {
            return true;
        }
        if (u.contains("MATURE")) {
            return true;
        }
        if (WORD_AO.matcher(ageRatingDisplay).find()) {
            return true;
        }
        // ESRB "M" alone (IGDB sometimes uses single-letter titles)
        if (u.contains("ESRB")) {
            int idx = u.lastIndexOf('·');
            if (idx >= 0) {
                String tail = u.substring(idx + 1).trim();
                if ("M".equals(tail)) {
                    return true;
                }
            }
        }
        // PEGI 18+
        if (u.contains("PEGI") && u.contains("18")) {
            return true;
        }
        // USK 18+
        if (u.contains("USK") && u.contains("18")) {
            return true;
        }
        // CERO Z (18+)
        if (u.contains("CERO") && (u.contains(" Z") || u.endsWith("Z"))) {
            return true;
        }
        // Australian R18+
        if (u.contains("R18+") || u.contains("R18 +")) {
            return true;
        }
        return false;
    }
}
