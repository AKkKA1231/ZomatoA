package com.zomato.recommendation.domain;

import java.math.BigDecimal;

/**
 * Structured filter inputs for Phase 2 retrieval. Populated from API requests later.
 */
public record FilterCriteria(
        String city,
        String cuisine,
        BigDecimal minRating,
        BudgetBand budgetBand
) {
}
