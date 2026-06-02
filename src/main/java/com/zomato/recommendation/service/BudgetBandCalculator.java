package com.zomato.recommendation.service;

import com.zomato.recommendation.domain.BudgetBand;
import com.zomato.recommendation.domain.Restaurant;
import com.zomato.recommendation.infrastructure.cache.DatasetMetadata;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BudgetBandCalculator {

    public Map<BudgetBand, DatasetMetadata.CostThreshold> computeThresholds(List<Integer> costs) {
        List<Integer> sorted = costs.stream()
                .filter(c -> c != null && c > 0)
                .sorted()
                .toList();

        if (sorted.isEmpty()) {
            return Map.of(
                    BudgetBand.LOW, new DatasetMetadata.CostThreshold(0, Integer.MAX_VALUE),
                    BudgetBand.MEDIUM, new DatasetMetadata.CostThreshold(0, Integer.MAX_VALUE),
                    BudgetBand.HIGH, new DatasetMetadata.CostThreshold(0, Integer.MAX_VALUE)
            );
        }

        int p33 = percentile(sorted, 33);
        int p66 = percentile(sorted, 66);

        if (p33 >= p66) {
            int value = sorted.get(sorted.size() / 2);
            p33 = value;
            p66 = value;
        }

        return Map.of(
                BudgetBand.LOW, new DatasetMetadata.CostThreshold(0, p33),
                BudgetBand.MEDIUM, new DatasetMetadata.CostThreshold(p33 + 1, p66),
                BudgetBand.HIGH, new DatasetMetadata.CostThreshold(p66 + 1, Integer.MAX_VALUE)
        );
    }

    public List<Restaurant> assignBands(List<Restaurant> restaurants, Map<BudgetBand, DatasetMetadata.CostThreshold> thresholds) {
        List<Restaurant> result = new ArrayList<>(restaurants.size());
        for (Restaurant restaurant : restaurants) {
            BudgetBand band = resolveBand(restaurant.costForTwo(), thresholds);
            result.add(new Restaurant(
                    restaurant.restaurantId(),
                    restaurant.name(),
                    restaurant.city(),
                    restaurant.location(),
                    restaurant.cuisines(),
                    restaurant.rating(),
                    restaurant.costForTwo(),
                    band,
                    restaurant.votes()
            ));
        }
        return result;
    }

    public BudgetBand resolveBand(Integer costForTwo, Map<BudgetBand, DatasetMetadata.CostThreshold> thresholds) {
        if (costForTwo == null || costForTwo <= 0) {
            return BudgetBand.MEDIUM;
        }
        for (BudgetBand band : List.of(BudgetBand.LOW, BudgetBand.MEDIUM, BudgetBand.HIGH)) {
            DatasetMetadata.CostThreshold threshold = thresholds.get(band);
            if (costForTwo >= threshold.minInclusive() && costForTwo <= threshold.maxInclusive()) {
                return band;
            }
        }
        return BudgetBand.HIGH;
    }

    private static int percentile(List<Integer> sorted, int percentile) {
        if (sorted.size() == 1) {
            return sorted.get(0);
        }
        double index = (percentile / 100.0) * (sorted.size() - 1);
        int lower = (int) Math.floor(index);
        int upper = (int) Math.ceil(index);
        if (lower == upper) {
            return sorted.get(lower);
        }
        double weight = index - lower;
        return (int) Math.round(sorted.get(lower) * (1 - weight) + sorted.get(upper) * weight);
    }
}
