package com.zomato.recommendation.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
        @Valid @NotNull DataProperties data,
        @Valid @NotNull RecommendationProperties recommendation,
        @Valid @NotNull LlmProperties llm
) {

    public record DataProperties(
            @NotBlank String cachePath,
            @NotBlank String datasetId,
            @NotBlank String rawPath,
            boolean autoIngestOnStartup
    ) {
    }

    public record RecommendationProperties(
            @Min(1) @Max(10) int topN,
            @Min(1) @Max(100) int shortlistMax,
            @Valid @NotNull PreRankProperties preRank
    ) {
    }

    public record PreRankProperties(
            @Positive double ratingWeight,
            @Positive double costWeight,
            @Positive double cuisineWeight
    ) {
    }

    public record LlmProperties(
            @NotBlank String provider,
            @NotBlank String baseUrl,
            @NotBlank String model,
            String apiKey,
            @Min(0) @Max(2) double temperature,
            @NotNull Duration timeout,
            @Min(0) @Max(5) int maxRetries
    ) {
    }
}
