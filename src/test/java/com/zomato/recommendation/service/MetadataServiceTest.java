package com.zomato.recommendation.service;

import com.zomato.recommendation.infrastructure.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetadataServiceTest {

    private MetadataService metadataService;
    private RestaurantRepository repository;

    @BeforeEach
    void setUp() {
        repository = mock(RestaurantRepository.class);
        metadataService = new MetadataService(repository);
    }

    @Test
    void shouldReturnDistinctCities() {
        when(repository.findDistinctCities()).thenReturn(List.of("Bangalore", "Delhi"));

        List<String> cities = metadataService.getDistinctCities();

        assertThat(cities).containsExactly("Bangalore", "Delhi");
    }

    @Test
    void shouldReturnDistinctCuisinesWithCity() {
        when(repository.findDistinctCuisines(Optional.of("Bangalore"))).thenReturn(List.of("Italian", "Chinese"));

        List<String> cuisines = metadataService.getDistinctCuisines(Optional.of("Bangalore"));

        assertThat(cuisines).containsExactly("Italian", "Chinese");
    }

    @Test
    void shouldReturnDistinctCuisinesWithoutCity() {
        when(repository.findDistinctCuisines(Optional.empty())).thenReturn(List.of("Italian", "Chinese", "Mexican"));

        List<String> cuisines = metadataService.getDistinctCuisines(Optional.empty());

        assertThat(cuisines).containsExactly("Italian", "Chinese", "Mexican");
    }
}
