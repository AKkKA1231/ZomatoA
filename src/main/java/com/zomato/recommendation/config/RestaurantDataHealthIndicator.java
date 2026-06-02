package com.zomato.recommendation.config;

import com.zomato.recommendation.infrastructure.repository.RestaurantRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class RestaurantDataHealthIndicator implements HealthIndicator {

    private final RestaurantRepository repository;

    public RestaurantDataHealthIndicator(RestaurantRepository repository) {
        this.repository = repository;
    }

    @Override
    public Health health() {
        if (!repository.isLoaded()) {
            return Health.down()
                    .withDetail("reason", "Restaurant cache not loaded")
                    .build();
        }
        return Health.up().withDetail("restaurantCount", repository.count()).build();
    }
}
