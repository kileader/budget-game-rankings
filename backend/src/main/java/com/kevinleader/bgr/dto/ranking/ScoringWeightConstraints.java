package com.kevinleader.bgr.dto.ranking;

import java.math.BigDecimal;

/** Allowed range for rating / playtime / price exponents in the value score (API + saved configs). */
public final class ScoringWeightConstraints {

    private ScoringWeightConstraints() {}

    public static final String MIN_STR = "0.0";
    public static final String MAX_STR = "2.0";

    public static final BigDecimal MIN = new BigDecimal(MIN_STR);
    public static final BigDecimal MAX = new BigDecimal(MAX_STR);
}
