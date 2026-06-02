package com.zomato.recommendation.service;

import com.zomato.recommendation.config.AppProperties;
import com.zomato.recommendation.domain.BudgetBand;
import com.zomato.recommendation.domain.IngestSummary;
import com.zomato.recommendation.domain.Restaurant;
import com.zomato.recommendation.infrastructure.cache.DatasetMetadata;
import com.zomato.recommendation.infrastructure.cache.RestaurantCacheStore;
import com.zomato.recommendation.infrastructure.dataset.HuggingFaceDatasetClient;
import com.zomato.recommendation.infrastructure.dataset.LocalDatasetReader;
import com.zomato.recommendation.infrastructure.dataset.RawRestaurantParser;
import com.zomato.recommendation.infrastructure.repository.InMemoryRestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class DatasetIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DatasetIngestionService.class);

    private final AppProperties appProperties;
    private final LocalDatasetReader localDatasetReader;
    private final HuggingFaceDatasetClient huggingFaceDatasetClient;
    private final RawRestaurantParser rawRestaurantParser;
    private final BudgetBandCalculator budgetBandCalculator;
    private final RestaurantCacheStore cacheStore;
    private final InMemoryRestaurantRepository repository;

    public DatasetIngestionService(
            AppProperties appProperties,
            LocalDatasetReader localDatasetReader,
            HuggingFaceDatasetClient huggingFaceDatasetClient,
            RawRestaurantParser rawRestaurantParser,
            BudgetBandCalculator budgetBandCalculator,
            RestaurantCacheStore cacheStore,
            InMemoryRestaurantRepository repository) {
        this.appProperties = appProperties;
        this.localDatasetReader = localDatasetReader;
        this.huggingFaceDatasetClient = huggingFaceDatasetClient;
        this.rawRestaurantParser = rawRestaurantParser;
        this.budgetBandCalculator = budgetBandCalculator;
        this.cacheStore = cacheStore;
        this.repository = repository;
    }

    public IngestSummary ingest() throws IOException {
        Path rawPath = Path.of(appProperties.data().rawPath());
        Path cachePath = Path.of(appProperties.data().cachePath());

        huggingFaceDatasetClient.ensureRawCsvAvailable(rawPath);

        LocalDatasetReader.RawDataset rawDataset = localDatasetReader.readFirstCsv(rawPath)
                .orElseThrow(() -> new IOException(
                        "No CSV found in " + rawPath.toAbsolutePath()
                                + ". Place a Zomato export CSV there or allow Hugging Face download."));

        RawRestaurantParser.ParseResult parsed = rawRestaurantParser.parseRows(
                rawDataset.rows(),
                rawDataset.headerMap());

        List<Integer> costs = parsed.restaurants().stream()
                .map(Restaurant::costForTwo)
                .filter(c -> c != null && c > 0)
                .toList();

        Map<BudgetBand, DatasetMetadata.CostThreshold> thresholds = budgetBandCalculator.computeThresholds(costs);
        List<Restaurant> withBands = budgetBandCalculator.assignBands(parsed.restaurants(), thresholds);

        DatasetMetadata metadata = new DatasetMetadata(
                Instant.now(),
                withBands.size(),
                appProperties.data().datasetId(),
                rawDataset.sourceName(),
                thresholds,
                parsed.skipped()
        );

        cacheStore.write(cachePath, withBands, metadata);
        repository.reload(withBands);

        log.info("Ingest complete: read={}, ingested={}, skipped={}",
                rawDataset.rows().size(), withBands.size(), parsed.skipped());

        return new IngestSummary(
                rawDataset.rows().size(),
                withBands.size(),
                parsed.skipped(),
                cachePath.toString(),
                rawDataset.sourceName()
        );
    }

    public void loadFromCache() throws IOException {
        String cachePath = appProperties.data().cachePath();
        List<Restaurant> restaurants = cacheStore.readRestaurants(cachePath);
        repository.reload(restaurants);
        log.info("Loaded {} restaurants from cache at {}", restaurants.size(), cachePath);
    }

    public boolean isCacheAvailable() {
        return cacheStore.exists(appProperties.data().cachePath());
    }
}
