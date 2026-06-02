package com.zomato.recommendation.api.dto;

import com.zomato.recommendation.domain.BudgetBand;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RecommendationRequestDto(
        @NotBlank(message = "Location cannot be blank")
        String location,

        @NotNull(message = "Budget band is required")
        BudgetBand budget,

        @NotBlank(message = "Cuisine cannot be blank")
        String cuisine,

        @DecimalMin(value = "0.0", message = "Min rating must be at least 0.0")
        @DecimalMax(value = "5.0", message = "Min rating cannot exceed 5.0")
        BigDecimal minRating,

        String additionalPreferences,

        @Min(value = 1, message = "topN must be at least 1")
        @Max(value = 10, message = "topN cannot exceed 10")
        Integer topN
) {
}
