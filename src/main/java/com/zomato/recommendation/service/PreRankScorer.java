package com.zomato.recommendation.service;

import com.zomato.recommendation.config.AppProperties;
import com.zomato.recommendation.domain.Restaurant;
import com.zomato.recommendation.domain.UserPreferences;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class PreRankScorer {

    private final AppProperties.PreRankProperties properties;

    public PreRankScorer(AppProperties appProperties) {
        this.properties = appProperties.recommendation().preRank();
    }

    public Comparator<Restaurant> getComparator(UserPreferences preferences) {
        return (r1, r2) -> {
            double score1 = calculateScore(r1, preferences);
            double score2 = calculateScore(r2, preferences);
            
            int comparison = Double.compare(score2, score1);
            if (comparison != 0) {
                return comparison;
            }
            
            // Tie-breakers
            int ratingCompare = 0;
            if (r1.rating() != null && r2.rating() != null) {
                ratingCompare = r2.rating().compareTo(r1.rating());
            } else if (r1.rating() != null) {
                ratingCompare = -1;
            } else if (r2.rating() != null) {
                ratingCompare = 1;
            }
            
            if (ratingCompare != 0) {
                return ratingCompare;
            }
            
            if (r1.name() != null && r2.name() != null) {
                return r1.name().compareTo(r2.name());
            }
            return 0;
        };
    }

    private double calculateScore(Restaurant restaurant, UserPreferences preferences) {
        double ratingScore = restaurant.rating() != null ? restaurant.rating().doubleValue() : 0.0;
        double normalizedRating = ratingScore / 5.0;

        double costScore = 0.0;
        if (restaurant.budgetBand() == preferences.budget()) {
            costScore = 1.0;
        }

        double cuisineScore = 0.0;
        if (preferences.cuisine() != null && restaurant.cuisines() != null) {
            boolean matchesCuisine = restaurant.cuisines().stream()
                    .anyMatch(c -> c.toLowerCase().contains(preferences.cuisine().toLowerCase()));
            if (matchesCuisine) {
                cuisineScore = 1.0;
            }
        }

        return (normalizedRating * properties.ratingWeight()) +
               (costScore * properties.costWeight()) +
               (cuisineScore * properties.cuisineWeight());
    }
}
