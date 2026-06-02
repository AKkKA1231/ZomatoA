package com.zomato.recommendation.infrastructure.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zomato.recommendation.domain.Restaurant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Component
public class RestaurantCacheStore {

    private static final Logger log = LoggerFactory.getLogger(RestaurantCacheStore.class);
    private static final String METADATA_FILE = "metadata.json";

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public RestaurantCacheStore(ResourceLoader resourceLoader) {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.resourceLoader = resourceLoader;
    }

    public Resource getResource(String location) {
        Resource resource = resourceLoader.getResource(location);
        if (resource.exists()) {
            return resource;
        }

        if (!location.startsWith("classpath:") && !location.startsWith("file:")) {
            Resource classpathResource = resourceLoader.getResource("classpath:" + location);
            if (classpathResource.exists()) {
                return classpathResource;
            }

            Resource fileResource = resourceLoader.getResource("file:" + location);
            if (fileResource.exists()) {
                return fileResource;
            }
        }
        return resource;
    }

    public boolean exists(String location) {
        return getResource(location).exists();
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

    public List<Restaurant> readRestaurants(String location) throws IOException {
        Resource resource = getResource(location);
        if (!resource.exists()) {
            throw new IOException("Cache resource not found: " + location);
        }
        try (InputStream is = resource.getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<>() {
            });
        }
    }

    public Optional<DatasetMetadata> readMetadata(String location) throws IOException {
        String metadataLocation;
        if (location.startsWith("classpath:")) {
            metadataLocation = location.substring(0, location.lastIndexOf('/') + 1) + METADATA_FILE;
        } else {
            int lastSlash = location.lastIndexOf('/');
            if (lastSlash != -1) {
                metadataLocation = location.substring(0, lastSlash + 1) + METADATA_FILE;
            } else {
                int lastBackslash = location.lastIndexOf('\\');
                if (lastBackslash != -1) {
                    metadataLocation = location.substring(0, lastBackslash + 1) + METADATA_FILE;
                } else {
                    metadataLocation = METADATA_FILE;
                }
            }
        }

        Resource metadataResource = getResource(metadataLocation);
        if (!metadataResource.exists()) {
            return Optional.empty();
        }
        try (InputStream is = metadataResource.getInputStream()) {
            return Optional.of(objectMapper.readValue(is, DatasetMetadata.class));
        }
    }
}
