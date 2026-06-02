package com.zomato.recommendation.service;

import com.zomato.recommendation.config.AppProperties;
import com.zomato.recommendation.infrastructure.cache.RestaurantCacheStore;
import com.zomato.recommendation.infrastructure.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DatasetIngestionServiceTest {

    @TempDir
    static Path tempDir;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("app.data.cache-path", () -> tempDir.resolve("restaurants.json").toString());
        registry.add("app.data.raw-path", () -> "src/test/resources/fixtures/raw");
    }

    @Autowired
    private DatasetIngestionService ingestionService;

    @Autowired
    private RestaurantRepository repository;

    @Autowired
    private RestaurantCacheStore cacheStore;

    @Autowired
    private AppProperties appProperties;

    @BeforeEach
    void clearRepository() {
        repository.reload(java.util.List.of());
    }

    @Test
    void ingestFromFixtureCsvWritesCacheAndLoadsRepository() throws Exception {
        var summary = ingestionService.ingest();

        assertThat(summary.rowsRead()).isEqualTo(20);
        assertThat(summary.rowsIngested()).isGreaterThanOrEqualTo(17);
        assertThat(summary.rowsSkipped()).isGreaterThanOrEqualTo(2);
        assertThat(repository.count()).isEqualTo(summary.rowsIngested());
        assertThat(Path.of(summary.cachePath())).exists();

        var metadata = cacheStore.readMetadata(appProperties.data().cachePath());
        assertThat(metadata).isPresent();
        assertThat(metadata.get().rowCount()).isEqualTo(summary.rowsIngested());
        assertThat(metadata.get().budgetThresholds()).containsKeys(
                com.zomato.recommendation.domain.BudgetBand.LOW,
                com.zomato.recommendation.domain.BudgetBand.MEDIUM,
                com.zomato.recommendation.domain.BudgetBand.HIGH
        );
    }

    @Test
    void restaurantIdIsStableAcrossIngests() throws Exception {
        ingestionService.ingest();
        String firstId = repository.findAll().get(0).restaurantId();

        repository.reload(java.util.List.of());
        ingestionService.ingest();

        assertThat(repository.findById(firstId)).isPresent();
    }

    @Test
    void parsesMultiCuisineField() throws Exception {
        ingestionService.ingest();
        boolean hasMultiple = repository.findAll().stream()
                .anyMatch(r -> r.cuisines().size() > 1);
        assertThat(hasMultiple).isTrue();
    }
}
