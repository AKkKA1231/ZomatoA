package com.zomato.recommendation.service;

import com.zomato.recommendation.api.dto.RecommendationResponseDto;
import com.zomato.recommendation.domain.FilterResult;
import com.zomato.recommendation.domain.LlmRecommendationDto;
import com.zomato.recommendation.domain.RecommendationResponse;
import com.zomato.recommendation.domain.Restaurant;
import com.zomato.recommendation.domain.UserPreferences;
import com.zomato.recommendation.infrastructure.llm.LlmGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(RecommendationOrchestrator.class);

    private final FilterService filterService;
    private final PromptBuilder promptBuilder;
    private final LlmGateway llmGateway;
    private final GroundingValidator groundingValidator;
    private final FallbackRecommendationService fallbackService;

    public RecommendationOrchestrator(
            FilterService filterService,
            PromptBuilder promptBuilder,
            LlmGateway llmGateway,
            GroundingValidator groundingValidator,
            FallbackRecommendationService fallbackService) {
        this.filterService = filterService;
        this.promptBuilder = promptBuilder;
        this.llmGateway = llmGateway;
        this.groundingValidator = groundingValidator;
        this.fallbackService = fallbackService;
    }

    public RecommendationResponseDto recommend(UserPreferences preferences, int topN) {
        FilterResult filterResult = filterService.filter(preferences);
        List<Restaurant> shortlist = filterResult.shortlist();

        if (shortlist.isEmpty()) {
            log.info("Retrieval phase returned zero candidates. Skipping LLM query.");
            return new RecommendationResponseDto(
                    List.of(),
                    "No restaurants matched your search criteria.",
                    filterResult.suggestions(),
                    new RecommendationResponseDto.MetaDto(0, false)
            );
        }

        int candidatesCount = shortlist.size();
        log.info("Retrieval phase returned {} candidates.", candidatesCount);

        try {
            String prompt = promptBuilder.build(preferences, shortlist, topN);
            RecommendationResponse llmResponse = llmGateway.complete(prompt);

            if (groundingValidator.isValid(llmResponse, shortlist, topN)) {
                log.info("LLM recommendations grounded successfully.");
                return enrichResponse(llmResponse, shortlist, false);
            } else {
                log.warn("Grounding validation failed for LLM recommendations. Triggering fallback.");
                RecommendationResponse fallbackResponse = fallbackService.getFallbackRecommendations(shortlist, preferences, topN);
                return enrichResponse(fallbackResponse, shortlist, true);
            }
        } catch (Exception e) {
            log.error("Error occurred during LLM recommendation pipeline. Triggering fallback.", e);
            RecommendationResponse fallbackResponse = fallbackService.getFallbackRecommendations(shortlist, preferences, topN);
            return enrichResponse(fallbackResponse, shortlist, true);
        }
    }

    private RecommendationResponseDto enrichResponse(RecommendationResponse rawResponse, List<Restaurant> shortlist, boolean degraded) {
        Map<String, Restaurant> shortlistMap = shortlist.stream()
                .collect(Collectors.toMap(Restaurant::restaurantId, Function.identity(), (r1, r2) -> r1));

        List<RecommendationResponseDto.RecommendationItemDto> enrichedItems = new ArrayList<>();
        for (LlmRecommendationDto dto : rawResponse.recommendations()) {
            Restaurant sourceOfTruth = shortlistMap.get(dto.restaurantId());
            if (sourceOfTruth != null) {
                // Enrich using repository fields as source of truth, keeping LLM explanation
                enrichedItems.add(new RecommendationResponseDto.RecommendationItemDto(
                        sourceOfTruth.restaurantId(),
                        sourceOfTruth.name(),
                        sourceOfTruth.cuisines(),
                        sourceOfTruth.rating(),
                        sourceOfTruth.costForTwo(),
                        dto.explanation()
                ));
            } else {
                // Fallback to DTO properties directly if not found in shortlist (though validator prevents this)
                enrichedItems.add(new RecommendationResponseDto.RecommendationItemDto(
                        dto.restaurantId(),
                        dto.name(),
                        dto.cuisines(),
                        dto.rating(),
                        dto.costForTwo(),
                        dto.explanation()
                ));
            }
        }

        return new RecommendationResponseDto(
                enrichedItems,
                rawResponse.summary(),
                List.of(),
                new RecommendationResponseDto.MetaDto(shortlist.size(), degraded)
        );
    }
}
