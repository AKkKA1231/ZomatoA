package com.zomato.recommendation.infrastructure.repository;

import com.zomato.recommendation.domain.BudgetBand;
import com.zomato.recommendation.domain.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRestaurantRepositoryTest {

    private InMemoryRestaurantRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRestaurantRepository();
    }

    @Test
    void reloadAndFindById() {
        Restaurant restaurant = sample("id-1", "Bangalore");
        repository.reload(List.of(restaurant));

        assertThat(repository.isLoaded()).isTrue();
        assertThat(repository.count()).isOne();
        assertThat(repository.findById("id-1")).contains(restaurant);
    }

    @Test
    void distinctCitiesAndCuisines() {
        repository.reload(List.of(
                sample("1", "Bangalore"),
                sample("2", "Delhi")
        ));

        assertThat(repository.findDistinctCities()).containsExactly("Bangalore", "Delhi");
        assertThat(repository.findDistinctCuisines(java.util.Optional.empty())).contains("Italian");
    }

    private static Restaurant sample(String id, String city) {
        return new Restaurant(
                id,
                "Test Restaurant",
                city,
                "Area",
                List.of("Italian"),
                new BigDecimal("4.5"),
                1000,
                BudgetBand.MEDIUM,
                100
        );
    }
}
