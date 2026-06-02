package com.zomato.recommendation.infrastructure.loader;

import com.zomato.recommendation.config.AppProperties;
import com.zomato.recommendation.service.DatasetIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class RestaurantDataLoader {

    private static final Logger log = LoggerFactory.getLogger(RestaurantDataLoader.class);

    private final AppProperties appProperties;
    private final DatasetIngestionService ingestionService;

    public RestaurantDataLoader(AppProperties appProperties, DatasetIngestionService ingestionService) {
        this.appProperties = appProperties;
        this.ingestionService = ingestionService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Path cachePath = Path.of(appProperties.data().cachePath());

        try {
            if (appProperties.data().autoIngestOnStartup()) {
                log.info("Auto-ingest on startup enabled");
                ingestionService.ingest();
                return;
            }

            if (Files.exists(cachePath)) {
                ingestionService.loadFromCache();
                return;
            }

            log.warn("Restaurant cache not found at {}. Load data via POST /api/v1/admin/ingest (dev profile) or run ingest.",
                    cachePath.toAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to load restaurant data on startup: {}", e.getMessage(), e);
        }
    }
}
