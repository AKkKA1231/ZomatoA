package com.zomato.recommendation.service;

import com.zomato.recommendation.config.AppProperties;
import com.zomato.recommendation.domain.BudgetBand;
import com.zomato.recommendation.domain.Restaurant;
import com.zomato.recommendation.domain.UserPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PreRankScorerTest {

    private PreRankScorer preRankScorer;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = mock(AppProperties.class);
        AppProperties.RecommendationProperties recommendationProperties = mock(AppProperties.RecommendationProperties.class);
        AppProperties.PreRankProperties preRankProperties = mock(AppProperties.PreRankProperties.class);

        when(appProperties.recommendation()).thenReturn(recommendationProperties);
        when(recommendationProperties.preRank()).thenReturn(preRankProperties);
        when(preRankProperties.ratingWeight()).thenReturn(0.5);
        when(preRankProperties.costWeight()).thenReturn(0.3);
        when(preRankProperties.cuisineWeight()).thenReturn(0.2);

        preRankScorer = new PreRankScorer(appProperties);
    }

    @Test
    void shouldRankBasedOnScore() {
        UserPreferences prefs = new UserPreferences("City", BudgetBand.MEDIUM, "Italian", null, null);

        Restaurant r1 = new Restaurant("1", "R1", "City", "Loc", List.of("Italian"), new BigDecimal("4.0"), 1000, BudgetBand.MEDIUM, 100);
        Restaurant r2 = new Restaurant("2", "R2", "City", "Loc", List.of("Chinese"), new BigDecimal("3.0"), 1000, BudgetBand.MEDIUM, 100);

        Comparator<Restaurant> comparator = preRankScorer.getComparator(prefs);
        List<Restaurant> sorted = java.util.stream.Stream.of(r2, r1)
                .sorted(comparator)
                .toList();

        assertThat(sorted.get(0).restaurantId()).isEqualTo("1"); // r1 has higher rating and matches cuisine
    }

    @Test
    void shouldTieBreakOnRating() {
        UserPreferences prefs = new UserPreferences("City", BudgetBand.MEDIUM, null, null, null);

        Restaurant r1 = new Restaurant("1", "R1", "City", "Loc", List.of(), new BigDecimal("4.5"), 1000, BudgetBand.MEDIUM, 100);
        Restaurant r2 = new Restaurant("2", "R2", "City", "Loc", List.of(), new BigDecimal("4.0"), 1000, BudgetBand.MEDIUM, 100);

        Comparator<Restaurant> comparator = preRankScorer.getComparator(prefs);
        List<Restaurant> sorted = java.util.stream.Stream.of(r2, r1)
                .sorted(comparator)
                .toList();

        assertThat(sorted.get(0).restaurantId()).isEqualTo("1");
    }

    @Test
    void shouldTieBreakOnName() {
        UserPreferences prefs = new UserPreferences("City", BudgetBand.MEDIUM, null, null, null);

        Restaurant r1 = new Restaurant("1", "A", "City", "Loc", List.of(), new BigDecimal("4.0"), 1000, BudgetBand.MEDIUM, 100);
        Restaurant r2 = new Restaurant("2", "B", "City", "Loc", List.of(), new BigDecimal("4.0"), 1000, BudgetBand.MEDIUM, 100);

        Comparator<Restaurant> comparator = preRankScorer.getComparator(prefs);
        List<Restaurant> sorted = java.util.stream.Stream.of(r2, r1)
                .sorted(comparator)
                .toList();

        assertThat(sorted.get(0).restaurantId()).isEqualTo("1");
    }
}
