package com.zomato.recommendation.service;

import com.zomato.recommendation.domain.LlmRecommendationDto;
import com.zomato.recommendation.domain.RecommendationResponse;
import com.zomato.recommendation.domain.Restaurant;
import com.zomato.recommendation.domain.UserPreferences;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FallbackRecommendationService {

    public RecommendationResponse getFallbackRecommendations(List<Restaurant> shortlist, UserPreferences preferences, int topN) {
        int limit = Math.min(topN, shortlist.size());
        List<LlmRecommendationDto> recommendations = new ArrayList<>();

        for (int i = 0; i < limit; i++) {
            Restaurant restaurant = shortlist.get(i);
            String explanation = String.format(
                    "Selected based on its high rating of %s in %s, offering %s cuisines matching your budget band.",
                    restaurant.rating(),
                    restaurant.location() != null ? restaurant.location() : restaurant.city(),
                    String.join(", ", restaurant.cuisines())
            );

            recommendations.add(new LlmRecommendationDto(
                    restaurant.restaurantId(),
                    restaurant.name(),
                    restaurant.cuisines(),
                    restaurant.rating(),
                    restaurant.costForTwo(),
                    explanation
            ));
        }

        String summary = String.format(
                "Curated %d restaurant options in %s based on ratings, cost, and cuisine match.",
                limit,
                preferences.city()
        );

        return new RecommendationResponse(recommendations, summary);
    }
}
