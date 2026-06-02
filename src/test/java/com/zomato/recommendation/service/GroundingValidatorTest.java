package com.zomato.recommendation.service;

import com.zomato.recommendation.domain.BudgetBand;
import com.zomato.recommendation.domain.LlmRecommendationDto;
import com.zomato.recommendation.domain.RecommendationResponse;
import com.zomato.recommendation.domain.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GroundingValidatorTest {

    private GroundingValidator validator;
    private List<Restaurant> shortlist;

    @BeforeEach
    void setUp() {
        validator = new GroundingValidator();
        shortlist = List.of(
                new Restaurant("1", "Res1", "Bangalore", "Loc1", List.of("Italian"), BigDecimal.valueOf(4.5), 1000, BudgetBand.MEDIUM, 100),
                new Restaurant("2", "Res2", "Bangalore", "Loc2", List.of("Italian"), BigDecimal.valueOf(4.2), 1200, BudgetBand.MEDIUM, 50)
        );
    }

    @Test
    void shouldValidateCorrectRecommendations() {
        RecommendationResponse response = new RecommendationResponse(
                List.of(new LlmRecommendationDto("1", "Res1", List.of("Italian"), BigDecimal.valueOf(4.5), 1000, "Good")),
                "Summary"
        );

        boolean result = validator.isValid(response, shortlist, 2);
        assertThat(result).isTrue();
    }

    @Test
    void shouldFailIfIdNotInShortlist() {
        RecommendationResponse response = new RecommendationResponse(
                List.of(new LlmRecommendationDto("99", "Res99", List.of("Italian"), BigDecimal.valueOf(4.5), 1000, "Good")),
                "Summary"
        );

        boolean result = validator.isValid(response, shortlist, 2);
        assertThat(result).isFalse();
    }

    @Test
    void shouldFailIfDuplicateIds() {
        RecommendationResponse response = new RecommendationResponse(
                List.of(
                        new LlmRecommendationDto("1", "Res1", List.of("Italian"), BigDecimal.valueOf(4.5), 1000, "Good"),
                        new LlmRecommendationDto("1", "Res1", List.of("Italian"), BigDecimal.valueOf(4.5), 1000, "Duplicate")
                ),
                "Summary"
        );

        boolean result = validator.isValid(response, shortlist, 2);
        assertThat(result).isFalse();
    }

    @Test
    void shouldFailIfCountExceedsTopN() {
        RecommendationResponse response = new RecommendationResponse(
                List.of(
                        new LlmRecommendationDto("1", "Res1", List.of("Italian"), BigDecimal.valueOf(4.5), 1000, "Good"),
                        new LlmRecommendationDto("2", "Res2", List.of("Italian"), BigDecimal.valueOf(4.2), 1200, "Good")
                ),
                "Summary"
        );

        boolean result = validator.isValid(response, shortlist, 1); // topN is 1
        assertThat(result).isFalse();
    }
}
