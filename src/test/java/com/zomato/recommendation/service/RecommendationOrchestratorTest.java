package com.zomato.recommendation.service;

import com.zomato.recommendation.api.dto.RecommendationResponseDto;
import com.zomato.recommendation.domain.*;
import com.zomato.recommendation.infrastructure.llm.LlmGateway;
import com.zomato.recommendation.infrastructure.llm.LlmException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class RecommendationOrchestratorTest {

    private FilterService filterService;
    private PromptBuilder promptBuilder;
    private LlmGateway llmGateway;
    private GroundingValidator groundingValidator;
    private FallbackRecommendationService fallbackService;

    private RecommendationOrchestrator orchestrator;

    private List<Restaurant> shortlist;

    @BeforeEach
    void setUp() {
        filterService = mock(FilterService.class);
        promptBuilder = mock(PromptBuilder.class);
        llmGateway = mock(LlmGateway.class);
        groundingValidator = mock(GroundingValidator.class);
        fallbackService = new FallbackRecommendationService();

        orchestrator = new RecommendationOrchestrator(
                filterService,
                promptBuilder,
                llmGateway,
                groundingValidator,
                fallbackService
        );

        shortlist = List.of(
                new Restaurant("1", "Res1", "Bangalore", "Loc1", List.of("Italian"), BigDecimal.valueOf(4.5), 1000, BudgetBand.MEDIUM, 100),
                new Restaurant("2", "Res2", "Bangalore", "Loc2", List.of("Italian"), BigDecimal.valueOf(4.2), 1200, BudgetBand.MEDIUM, 50)
        );
    }

    @Test
    void shouldProcessRecommendationsSuccessfullyWhenLlmSucceeds() {
        UserPreferences preferences = new UserPreferences("Bangalore", BudgetBand.MEDIUM, "Italian", BigDecimal.valueOf(4.0), "None");

        when(filterService.filter(preferences)).thenReturn(new FilterResult(shortlist, List.of()));
        when(promptBuilder.build(any(), any(), anyInt())).thenReturn("Mock Prompt");

        RecommendationResponse llmResponse = new RecommendationResponse(
                List.of(new LlmRecommendationDto("1", "Res1", List.of("Italian"), BigDecimal.valueOf(4.5), 1000, "Excellent choice")),
                "Curated summary"
        );
        when(llmGateway.complete("Mock Prompt")).thenReturn(llmResponse);
        when(groundingValidator.isValid(llmResponse, shortlist, 2)).thenReturn(true);

        RecommendationResponseDto responseDto = orchestrator.recommend(preferences, 2);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.summary()).isEqualTo("Curated summary");
        assertThat(responseDto.recommendations()).hasSize(1);
        assertThat(responseDto.recommendations().get(0).name()).isEqualTo("Res1");
        assertThat(responseDto.meta().degraded()).isFalse();
        assertThat(responseDto.meta().candidatesConsidered()).isEqualTo(2);

        verify(llmGateway, times(1)).complete(anyString());
    }

    @Test
    void shouldFallbackWhenLlmFails() {
        UserPreferences preferences = new UserPreferences("Bangalore", BudgetBand.MEDIUM, "Italian", BigDecimal.valueOf(4.0), "None");

        when(filterService.filter(preferences)).thenReturn(new FilterResult(shortlist, List.of()));
        when(promptBuilder.build(any(), any(), anyInt())).thenReturn("Mock Prompt");
        when(llmGateway.complete("Mock Prompt")).thenThrow(new LlmException("Timeout"));

        RecommendationResponseDto responseDto = orchestrator.recommend(preferences, 2);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.recommendations()).hasSize(2); // Top 2 candidates from shortlist
        assertThat(responseDto.meta().degraded()).isTrue();
        assertThat(responseDto.meta().candidatesConsidered()).isEqualTo(2);
    }

    @Test
    void shouldReturnSuggestionsWithoutCallingLlmWhenShortlistIsEmpty() {
        UserPreferences preferences = new UserPreferences("Bangalore", BudgetBand.MEDIUM, "Italian", BigDecimal.valueOf(4.0), "None");
        List<String> suggestions = List.of("Try changing rating", "Try another city");

        when(filterService.filter(preferences)).thenReturn(FilterResult.empty(suggestions));

        RecommendationResponseDto responseDto = orchestrator.recommend(preferences, 2);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.recommendations()).isEmpty();
        assertThat(responseDto.suggestions()).containsExactlyElementsOf(suggestions);
        assertThat(responseDto.meta().candidatesConsidered()).isZero();
        assertThat(responseDto.meta().degraded()).isFalse();

        verifyNoInteractions(promptBuilder);
        verifyNoInteractions(llmGateway);
    }
}
