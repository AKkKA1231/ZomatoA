package com.zomato.recommendation.domain;

import java.math.BigDecimal;

/**
 * Represents the user's preferences for a recommendation request.
 */
public record UserPreferences(
        String city,
        BudgetBand budget,
        String cuisine,
        BigDecimal minRating,
        String additionalPreferences
) {
}
