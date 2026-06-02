package com.zomato.recommendation.service;

import com.zomato.recommendation.domain.LlmRecommendationDto;
import com.zomato.recommendation.domain.RecommendationResponse;
import com.zomato.recommendation.domain.Restaurant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates that the LLM recommendations are grounded: only existing candidate IDs,
 * no duplicates, and size respects configured topN boundaries.
 */
@Service
public class GroundingValidator {

    private static final Logger log = LoggerFactory.getLogger(GroundingValidator.class);

    public boolean isValid(RecommendationResponse response, List<Restaurant> shortlist, int topN) {
        if (response == null) {
            log.warn("Grounding validation failed: response is null");
            return false;
        }

        List<LlmRecommendationDto> recommendations = response.recommendations();
        if (recommendations == null) {
            log.warn("Grounding validation failed: recommendations list is null");
            return false;
        }

        if (recommendations.size() > topN) {
            log.warn("Grounding validation failed: recommended count {} exceeds requested topN {}", recommendations.size(), topN);
            return false;
        }

        Set<String> allowedIds = shortlist.stream()
                .map(Restaurant::restaurantId)
                .collect(Collectors.toSet());

        Set<String> seenIds = new HashSet<>();

        for (LlmRecommendationDto rec : recommendations) {
            String recId = rec.restaurantId();
            if (recId == null || !allowedIds.contains(recId)) {
                log.warn("Grounding validation failed: recommended restaurant ID {} not in candidate shortlist", recId);
                return false;
            }
            if (seenIds.contains(recId)) {
                log.warn("Grounding validation failed: duplicate recommended restaurant ID {}", recId);
                return false;
            }
            seenIds.add(recId);
        }

        return true;
    }
}
