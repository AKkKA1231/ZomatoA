package com.zomato.recommendation.domain;

import java.math.BigDecimal;
import java.util.List;

public record Restaurant(
        String restaurantId,
        String name,
        String city,
        String location,
        List<String> cuisines,
        BigDecimal rating,
        Integer costForTwo,
        BudgetBand budgetBand,
        Integer votes
) {
}
