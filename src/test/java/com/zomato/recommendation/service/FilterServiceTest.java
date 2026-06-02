package com.zomato.recommendation.service;

import com.zomato.recommendation.config.AppProperties;
import com.zomato.recommendation.domain.BudgetBand;
import com.zomato.recommendation.domain.FilterResult;
import com.zomato.recommendation.domain.Restaurant;
import com.zomato.recommendation.domain.UserPreferences;
import com.zomato.recommendation.infrastructure.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilterServiceTest {

    private FilterService filterService;
    private RestaurantRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(RestaurantRepository.class);
        PreRankScorer preRankScorer = mock(PreRankScorer.class);
        
        AppProperties appProperties = mock(AppProperties.class);
        AppProperties.RecommendationProperties recommendationProperties = mock(AppProperties.RecommendationProperties.class);
        
        when(appProperties.recommendation()).thenReturn(recommendationProperties);
        when(recommendationProperties.shortlistMax()).thenReturn(5);
        
        when(preRankScorer.getComparator(any())).thenReturn((r1, r2) -> 0);

        filterService = new FilterService(repository, preRankScorer, appProperties);
    }

    @Test
    void shouldFilterAndReturnShortlist() {
        Restaurant r1 = new Restaurant("1", "R1", "Bangalore", "Loc", List.of("Italian"), new BigDecimal("4.5"), 1000, BudgetBand.MEDIUM, 100);
        Restaurant r2 = new Restaurant("2", "R2", "Delhi", "Loc", List.of("Italian"), new BigDecimal("4.5"), 1000, BudgetBand.MEDIUM, 100);
        
        when(repository.findAll()).thenReturn(List.of(r1, r2));

        UserPreferences prefs = new UserPreferences("Bangalore", BudgetBand.MEDIUM, "Italian", new BigDecimal("4.0"), null);
        FilterResult result = filterService.filter(prefs);

        assertThat(result.shortlist()).hasSize(1);
        assertThat(result.shortlist().get(0).restaurantId()).isEqualTo("1");
        assertThat(result.suggestions()).isEmpty();
    }

    @Test
    void shouldReturnSuggestionsOnEmptyResult() {
        when(repository.findAll()).thenReturn(List.of());

        UserPreferences prefs = new UserPreferences("Bangalore", BudgetBand.MEDIUM, "Italian", new BigDecimal("4.0"), null);
        FilterResult result = filterService.filter(prefs);

        assertThat(result.shortlist()).isEmpty();
        assertThat(result.suggestions()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldFilterByCity() {
        Restaurant r1 = new Restaurant("1", "R1", "Bangalore", "Loc", List.of(), null, 0, null, 0);
        Restaurant r2 = new Restaurant("2", "R2", "Delhi", "Loc", List.of(), null, 0, null, 0);
        when(repository.findAll()).thenReturn(List.of(r1, r2));

        UserPreferences prefs = new UserPreferences("Bangalore", null, null, null, null);
        FilterResult result = filterService.filter(prefs);

        assertThat(result.shortlist()).hasSize(1);
        assertThat(result.shortlist().get(0).restaurantId()).isEqualTo("1");
    }

    @Test
    void shouldFilterByCuisine() {
        Restaurant r1 = new Restaurant("1", "R1", "Bangalore", "Loc", List.of("Italian"), null, 0, null, 0);
        Restaurant r2 = new Restaurant("2", "R2", "Bangalore", "Loc", List.of("Chinese"), null, 0, null, 0);
        when(repository.findAll()).thenReturn(List.of(r1, r2));

        UserPreferences prefs = new UserPreferences(null, null, "Italian", null, null);
        FilterResult result = filterService.filter(prefs);

        assertThat(result.shortlist()).hasSize(1);
        assertThat(result.shortlist().get(0).restaurantId()).isEqualTo("1");
    }

    @Test
    void shouldFilterByMinRating() {
        Restaurant r1 = new Restaurant("1", "R1", "Bangalore", "Loc", List.of(), new BigDecimal("4.5"), 0, null, 0);
        Restaurant r2 = new Restaurant("2", "R2", "Bangalore", "Loc", List.of(), new BigDecimal("3.5"), 0, null, 0);
        when(repository.findAll()).thenReturn(List.of(r1, r2));

        UserPreferences prefs = new UserPreferences(null, null, null, new BigDecimal("4.0"), null);
        FilterResult result = filterService.filter(prefs);

        assertThat(result.shortlist()).hasSize(1);
        assertThat(result.shortlist().get(0).restaurantId()).isEqualTo("1");
    }

    @Test
    void shouldFilterByBudgetBand() {
        Restaurant r1 = new Restaurant("1", "R1", "Bangalore", "Loc", List.of(), null, 0, BudgetBand.LOW, 0);
        Restaurant r2 = new Restaurant("2", "R2", "Bangalore", "Loc", List.of(), null, 0, BudgetBand.HIGH, 0);
        when(repository.findAll()).thenReturn(List.of(r1, r2));

        UserPreferences prefs = new UserPreferences(null, BudgetBand.LOW, null, null, null);
        FilterResult result = filterService.filter(prefs);

        assertThat(result.shortlist()).hasSize(1);
        assertThat(result.shortlist().get(0).restaurantId()).isEqualTo("1");
    }
}
