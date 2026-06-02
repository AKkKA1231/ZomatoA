package com.zomato.recommendation.domain;

public record IngestSummary(
        int rowsRead,
        int rowsIngested,
        int rowsSkipped,
        String cachePath,
        String sourceDescription
) {
}
