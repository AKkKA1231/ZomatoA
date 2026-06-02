package com.zomato.recommendation.domain;

import java.util.List;

/**
 * Root domain class representing the structured response from the LLM.
 */
public record RecommendationResponse(
        List<LlmRecommendationDto> recommendations,
        String summary
) {
}
