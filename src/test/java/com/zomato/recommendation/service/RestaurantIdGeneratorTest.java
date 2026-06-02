package com.zomato.recommendation.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestaurantIdGeneratorTest {

    private final RestaurantIdGenerator generator = new RestaurantIdGenerator();

    @Test
    void generatesStableIdForSameNameAndCity() {
        String first = generator.generate("Trattoria Roma", "Bangalore");
        String second = generator.generate("Trattoria Roma", "Bangalore");
        assertThat(first).isEqualTo(second);
        assertThat(first).hasSize(16);
    }

    @Test
    void generatesDifferentIdsForDifferentRestaurants() {
        String a = generator.generate("Trattoria Roma", "Bangalore");
        String b = generator.generate("Spice Garden", "Bangalore");
        assertThat(a).isNotEqualTo(b);
    }
}
