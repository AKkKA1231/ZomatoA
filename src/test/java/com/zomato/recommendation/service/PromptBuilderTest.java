package com.zomato.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zomato.recommendation.domain.BudgetBand;
import com.zomato.recommendation.domain.Restaurant;
import com.zomato.recommendation.domain.UserPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderTest {

    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder(new ObjectMapper());
    }

    @Test
    void shouldBuildPromptWithAllPreferencesAndCandidates() {
        UserPreferences preferences = new UserPreferences(
                "Bangalore",
                BudgetBand.MEDIUM,
                "Italian",
                BigDecimal.valueOf(4.0),
                "Outdoor seating preferred"
        );

        Restaurant restaurant = new Restaurant(
                "123",
                "Toscano",
                "Bangalore",
                "UB City",
                List.of("Italian", "Pizza"),
                BigDecimal.valueOf(4.2),
                1500,
                BudgetBand.MEDIUM,
                250
        );

        String prompt = promptBuilder.build(preferences, List.of(restaurant), 3);

        assertThat(prompt)
                .contains("Bangalore")
                .contains("MEDIUM")
                .contains("Italian")
                .contains("4.0")
                .contains("Outdoor seating preferred")
                .contains("Toscano")
                .contains("123")
                .contains("3");
    }
}
