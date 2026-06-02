package com.zomato.recommendation.infrastructure.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zomato.recommendation.domain.Restaurant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
public class RestaurantCacheStore {

    private static final Logger log = LoggerFactory.getLogger(RestaurantCacheStore.class);
    private static final String METADATA_FILE = "metadata.json";

    private final ObjectMapper objectMapper;

    public RestaurantCacheStore() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void write(Path cachePath, List<Restaurant> restaurants, DatasetMetadata metadata) throws IOException {
        Path parent = cachePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(cachePath.toFile(), restaurants);
        Path metadataPath = parent != null ? parent.resolve(METADATA_FILE) : Path.of(METADATA_FILE);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(metadataPath.toFile(), metadata);
        log.info("Wrote {} restaurants to {} and metadata to {}", restaurants.size(), cachePath, metadataPath);
    }

    public List<Restaurant> readRestaurants(Path cachePath) throws IOException {
        if (!Files.exists(cachePath)) {
            throw new IOException("Cache file not found: " + cachePath.toAbsolutePath());
        }
        return objectMapper.readValue(cachePath.toFile(), new TypeReference<>() {
        });
    }

    public Optional<DatasetMetadata> readMetadata(Path cachePath) throws IOException {
        Path metadataPath = cachePath.getParent().resolve(METADATA_FILE);
        if (!Files.exists(metadataPath)) {
            return Optional.empty();
        }
        return Optional.of(objectMapper.readValue(metadataPath.toFile(), DatasetMetadata.class));
    }
}
