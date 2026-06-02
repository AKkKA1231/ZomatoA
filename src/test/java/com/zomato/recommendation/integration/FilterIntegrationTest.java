package com.zomato.recommendation.integration;

import com.zomato.recommendation.domain.BudgetBand;
import com.zomato.recommendation.domain.FilterResult;
import com.zomato.recommendation.domain.UserPreferences;
import com.zomato.recommendation.service.FilterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FilterIntegrationTest {

    @Autowired
    private FilterService filterService;

    @Test
    void shouldFilterAgainstLoadedFixtureRepository() {
        // Search for Italian in Bangalore with MEDIUM budget
        UserPreferences prefs = new UserPreferences("Bangalore", BudgetBand.MEDIUM, "Italian", new BigDecimal("4.0"), null);
        FilterResult result = filterService.filter(prefs);

        assertThat(result.shortlist()).isNotEmpty();
        assertThat(result.suggestions()).isEmpty();
        
        // Assert all elements match criteria
        result.shortlist().forEach(r -> {
            assertThat(r.city()).containsIgnoringCase("Bangalore");
            assertThat(r.cuisines().stream().anyMatch(c -> c.toLowerCase().contains("italian"))).isTrue();
            assertThat(r.rating()).isGreaterThanOrEqualTo(new BigDecimal("4.0"));
            assertThat(r.budgetBand()).isEqualTo(BudgetBand.MEDIUM);
        });
    }

    @Test
    void shouldReturnSuggestionsForImpossibleFilter() {
        // Find something extremely unlikely to be in the fixture
        UserPreferences prefs = new UserPreferences("NowhereCity", BudgetBand.HIGH, "AlienCuisine", new BigDecimal("5.0"), null);
        FilterResult result = filterService.filter(prefs);

        assertThat(result.shortlist()).isEmpty();
        assertThat(result.suggestions()).isNotEmpty();
    }
}
