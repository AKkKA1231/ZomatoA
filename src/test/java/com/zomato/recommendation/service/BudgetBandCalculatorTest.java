package com.zomato.recommendation.service;

import com.zomato.recommendation.domain.BudgetBand;
import com.zomato.recommendation.domain.Restaurant;
import com.zomato.recommendation.infrastructure.cache.DatasetMetadata;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BudgetBandCalculatorTest {

    private final BudgetBandCalculator calculator = new BudgetBandCalculator();

    @Test
    void computesThresholdsForVariedCosts() {
        Map<BudgetBand, DatasetMetadata.CostThreshold> thresholds = calculator.computeThresholds(
                List.of(300, 600, 900, 1200, 2000, 3500));

        assertThat(thresholds).containsKeys(BudgetBand.LOW, BudgetBand.MEDIUM, BudgetBand.HIGH);
        assertThat(thresholds.get(BudgetBand.LOW).maxInclusive())
                .isLessThanOrEqualTo(thresholds.get(BudgetBand.MEDIUM).minInclusive());
    }

    @Test
    void handlesUniformCosts() {
        Map<BudgetBand, DatasetMetadata.CostThreshold> thresholds = calculator.computeThresholds(
                List.of(500, 500, 500));

        assertThat(thresholds).isNotEmpty();
        BudgetBand band = calculator.resolveBand(500, thresholds);
        assertThat(band).isIn(BudgetBand.LOW, BudgetBand.MEDIUM, BudgetBand.HIGH);
    }

    @Test
    void assignsBandToRestaurant() {
        Map<BudgetBand, DatasetMetadata.CostThreshold> thresholds = calculator.computeThresholds(
                List.of(300, 600, 1200, 3000));

        Restaurant lowCost = restaurant(300);
        List<Restaurant> assigned = calculator.assignBands(List.of(lowCost), thresholds);

        assertThat(assigned.get(0).budgetBand()).isEqualTo(BudgetBand.LOW);
    }

    @Test
    void nullCostDefaultsToMedium() {
        Map<BudgetBand, DatasetMetadata.CostThreshold> thresholds = calculator.computeThresholds(List.of(500, 1000));
        assertThat(calculator.resolveBand(null, thresholds)).isEqualTo(BudgetBand.MEDIUM);
    }

    private static Restaurant restaurant(int cost) {
        return new Restaurant(
                "id",
                "Test",
                "Bangalore",
                "Area",
                List.of("Italian"),
                new BigDecimal("4.0"),
                cost,
                null,
                10
        );
    }
}
