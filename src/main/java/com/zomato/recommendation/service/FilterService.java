package com.zomato.recommendation.service;

import com.zomato.recommendation.config.AppProperties;
import com.zomato.recommendation.domain.FilterResult;
import com.zomato.recommendation.domain.Restaurant;
import com.zomato.recommendation.domain.UserPreferences;
import com.zomato.recommendation.infrastructure.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilterService {

    private final RestaurantRepository repository;
    private final PreRankScorer preRankScorer;
    private final int shortlistMax;

    public FilterService(RestaurantRepository repository, PreRankScorer preRankScorer, AppProperties appProperties) {
        this.repository = repository;
        this.preRankScorer = preRankScorer;
        this.shortlistMax = appProperties.recommendation().shortlistMax();
    }

    public FilterResult filter(UserPreferences preferences) {
        List<Restaurant> matches = repository.findAll().stream()
                .filter(r -> matchCity(r, preferences))
                .filter(r -> matchCuisine(r, preferences))
                .filter(r -> matchRating(r, preferences))
                .filter(r -> matchBudget(r, preferences))
                .sorted(preRankScorer.getComparator(preferences))
                .limit(shortlistMax)
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            return FilterResult.empty(generateSuggestions(preferences));
        }

        return new FilterResult(matches, List.of());
    }

    private boolean matchCity(Restaurant r, UserPreferences p) {
        if (p.city() == null || p.city().isBlank() || p.city().equalsIgnoreCase("any")) return true;
        if (r.city() == null) return false;
        return r.city().toLowerCase().contains(p.city().toLowerCase());
    }

    private boolean matchCuisine(Restaurant r, UserPreferences p) {
        if (p.cuisine() == null || p.cuisine().isBlank() || p.cuisine().equalsIgnoreCase("any")) return true;
        if (r.cuisines() == null) return false;
        return r.cuisines().stream()
                .anyMatch(c -> c.toLowerCase().contains(p.cuisine().toLowerCase()));
    }

    private boolean matchRating(Restaurant r, UserPreferences p) {
        if (p.minRating() == null) return true;
        if (r.rating() == null) return false;
        return r.rating().compareTo(p.minRating()) >= 0;
    }

    private boolean matchBudget(Restaurant r, UserPreferences p) {
        if (p.budget() == null) return true;
        return p.budget() == r.budgetBand();
    }

    private List<String> generateSuggestions(UserPreferences preferences) {
        List<String> suggestions = new ArrayList<>();
        if (preferences.minRating() != null && preferences.minRating().doubleValue() > 0) {
            suggestions.add("Try lowering the minimum rating requirement.");
        }
        if (preferences.cuisine() != null && !preferences.cuisine().isBlank()) {
            suggestions.add("Explore other cuisines in this city.");
        }
        if (preferences.budget() != null) {
            suggestions.add("Consider changing your budget band.");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Try relaxing your search criteria.");
            suggestions.add("Check if the city is available in our dataset.");
        }
        if (suggestions.size() == 1) {
            suggestions.add("Try relaxing your search criteria.");
        }
        return suggestions;
    }
}
