package com.zomato.recommendation.infrastructure.repository;

import com.zomato.recommendation.domain.Restaurant;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryRestaurantRepository implements RestaurantRepository {

    private final Map<String, Restaurant> byId = new ConcurrentHashMap<>();

    @Override
    public Optional<Restaurant> findById(String restaurantId) {
        return Optional.ofNullable(byId.get(restaurantId));
    }

    @Override
    public List<Restaurant> findAll() {
        return List.copyOf(byId.values());
    }

    @Override
    public int count() {
        return byId.size();
    }

    @Override
    public boolean isLoaded() {
        return !byId.isEmpty();
    }

    @Override
    public void reload(Collection<Restaurant> restaurants) {
        byId.clear();
        for (Restaurant restaurant : restaurants) {
            byId.put(restaurant.restaurantId(), restaurant);
        }
    }

    public List<String> findDistinctCities() {
        return byId.values().stream()
                .map(Restaurant::city)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> findDistinctCuisines(Optional<String> city) {
        return byId.values().stream()
                .filter(r -> city.isEmpty() || r.city().equalsIgnoreCase(city.get()))
                .flatMap(r -> r.cuisines().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
