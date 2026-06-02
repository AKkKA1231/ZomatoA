package com.zomato.recommendation.infrastructure.repository;

import com.zomato.recommendation.domain.Restaurant;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RestaurantRepository {

    Optional<Restaurant> findById(String restaurantId);

    List<Restaurant> findAll();

    int count();

    boolean isLoaded();

    void reload(Collection<Restaurant> restaurants);

    List<String> findDistinctCities();

    List<String> findDistinctCuisines(Optional<String> city);
}
