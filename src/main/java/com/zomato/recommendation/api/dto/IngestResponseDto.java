package com.zomato.recommendation.api.dto;

import com.zomato.recommendation.domain.IngestSummary;

public record IngestResponseDto(
        int rowsRead,
        int rowsIngested,
        int rowsSkipped,
        String cachePath,
        String sourceDescription
) {
    public static IngestResponseDto from(IngestSummary summary) {
        return new IngestResponseDto(
                summary.rowsRead(),
                summary.rowsIngested(),
                summary.rowsSkipped(),
                summary.cachePath(),
                summary.sourceDescription()
        );
    }
}
