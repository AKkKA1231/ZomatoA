package com.zomato.recommendation.infrastructure.loader;

import com.zomato.recommendation.infrastructure.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.data.cache-path=src/test/resources/fixtures/restaurants-small.json",
        "app.data.raw-path=src/test/resources/fixtures/raw",
        "app.data.auto-ingest-on-startup=false"
})
class RestaurantDataLoaderTest {

    @Autowired
    private RestaurantRepository repository;

    @Test
    void loadsRestaurantsFromClasspathFixtureOnStartup() {
        assertThat(repository.isLoaded()).isTrue();
        assertThat(repository.count()).isEqualTo(18);
        assertThat(repository.findById(repository.findAll().get(0).restaurantId())).isPresent();
    }
}
