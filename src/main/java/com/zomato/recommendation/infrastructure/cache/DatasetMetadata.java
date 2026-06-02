package com.zomato.recommendation.infrastructure.cache;

import com.zomato.recommendation.domain.BudgetBand;

import java.time.Instant;
import java.util.Map;

public record DatasetMetadata(
        Instant ingestedAt,
        int rowCount,
        String datasetId,
        String sourceDescription,
        Map<BudgetBand, CostThreshold> budgetThresholds,
        int rowsSkipped
) {

    public record CostThreshold(int minInclusive, int maxInclusive) {
    }
}
