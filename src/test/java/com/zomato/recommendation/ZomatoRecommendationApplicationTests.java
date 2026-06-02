package com.zomato.recommendation;

import com.zomato.recommendation.config.AppProperties;
import com.zomato.recommendation.infrastructure.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ZomatoRecommendationApplicationTests {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    void contextLoads() {
        assertThat(appProperties).isNotNull();
        assertThat(appProperties.data().cachePath()).contains("restaurants-small.json");
        assertThat(appProperties.recommendation().topN()).isEqualTo(3);
        assertThat(appProperties.llm().provider()).isEqualTo("groq");
        assertThat(restaurantRepository.isLoaded()).isTrue();
        assertThat(restaurantRepository.count()).isPositive();
    }
}
