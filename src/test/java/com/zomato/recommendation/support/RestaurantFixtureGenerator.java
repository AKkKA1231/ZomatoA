package com.zomato.recommendation.support;

import com.zomato.recommendation.service.DatasetIngestionService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * One-off utility to refresh {@code src/test/resources/fixtures/restaurants-small.json}.
 * Run: {@code mvn -q -DskipTests exec:java -Dexec.classpathScope=test -Dexec.mainClass=com.zomato.recommendation.support.RestaurantFixtureGenerator}
 */
public class RestaurantFixtureGenerator {

    public static void main(String[] args) throws Exception {
        System.setProperty("spring.profiles.active", "test");
        System.setProperty("app.data.cache-path", "src/test/resources/fixtures/restaurants-small.json");
        System.setProperty("app.data.raw-path", "src/test/resources/fixtures/raw");
        System.setProperty("app.data.auto-ingest-on-startup", "false");

        ConfigurableApplicationContext context =
                SpringApplication.run(com.zomato.recommendation.ZomatoRecommendationApplication.class, args);

        DatasetIngestionService ingestionService = context.getBean(DatasetIngestionService.class);
        var summary = ingestionService.ingest();
        System.out.println("Wrote fixture: " + summary.rowsIngested() + " restaurants -> " + summary.cachePath());
        context.close();
    }
}
