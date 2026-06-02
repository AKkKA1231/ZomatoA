package com.zomato.recommendation.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a single restaurant recommendation returned by the LLM.
 */
public record LlmRecommendationDto(
        String restaurantId,
        String name,
        List<String> cuisines,
        BigDecimal rating,
        Integer costForTwo,
        String explanation
) {
}
