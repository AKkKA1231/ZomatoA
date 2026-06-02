package com.zomato.recommendation.service;

import com.zomato.recommendation.infrastructure.repository.RestaurantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MetadataService {

    private final RestaurantRepository repository;

    public MetadataService(RestaurantRepository repository) {
        this.repository = repository;
    }

    public List<String> getDistinctCities() {
        return repository.findDistinctCities();
    }

    public List<String> getDistinctCuisines(Optional<String> city) {
        return repository.findDistinctCuisines(city);
    }
}
