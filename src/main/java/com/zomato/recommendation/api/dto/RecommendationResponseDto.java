package com.zomato.recommendation.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record RecommendationResponseDto(
        List<RecommendationItemDto> recommendations,
        String summary,
        List<String> suggestions,
        MetaDto meta
) {
    public record MetaDto(
            int candidatesConsidered,
            boolean degraded
    ) {}

    public record RecommendationItemDto(
            String restaurantId,
            String name,
            List<String> cuisines,
            BigDecimal rating,
            Integer costForTwo,
            String explanation
    ) {}
}
