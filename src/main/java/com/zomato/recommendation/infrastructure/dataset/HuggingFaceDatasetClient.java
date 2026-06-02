package com.zomato.recommendation.infrastructure.dataset;

import com.zomato.recommendation.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

/**
 * Attempts to download dataset CSV from Hugging Face Hub into {@code data/raw/}.
 * Falls back to existing local files when download is unavailable.
 */
@Component
public class HuggingFaceDatasetClient {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceDatasetClient.class);

    private final AppProperties appProperties;
    private final WebClient webClient;

    public HuggingFaceDatasetClient(AppProperties appProperties, WebClient.Builder webClientBuilder) {
        this.appProperties = appProperties;
        this.webClient = webClientBuilder
                .baseUrl("https://huggingface.co")
                .build();
    }

    /**
     * Ensures a CSV exists under raw path. Returns path to CSV if available.
     */
    public Optional<Path> ensureRawCsvAvailable(Path rawDirectory) throws IOException {
        Files.createDirectories(rawDirectory);

        Optional<Path> existing = findLocalCsv(rawDirectory);
        if (existing.isPresent()) {
            return existing;
        }

        String datasetId = appProperties.data().datasetId();
        String repoPath = "/datasets/" + datasetId + "/resolve/main/";
        String[] candidates = {"zomato.csv", "Zomato.csv", "restaurants.csv", "data.csv", "train.csv"};

        for (String fileName : candidates) {
            Path target = rawDirectory.resolve(fileName);
            try {
                byte[] body = webClient.get()
                        .uri(repoPath + fileName)
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .block(Duration.ofMinutes(5));

                if (body != null && body.length > 0) {
                    Files.write(target, body);
                    log.info("Downloaded {} from Hugging Face ({} bytes)", fileName, body.length);
                    return Optional.of(target);
                }
            } catch (Exception e) {
                log.debug("Could not download {} from Hugging Face: {}", fileName, e.getMessage());
            }
        }

        log.warn("No local CSV in {} and Hugging Face download failed for dataset {}", rawDirectory, datasetId);
        return Optional.empty();
    }

    private Optional<Path> findLocalCsv(Path rawDirectory) throws IOException {
        if (!Files.isDirectory(rawDirectory)) {
            return Optional.empty();
        }
        try (var stream = Files.list(rawDirectory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".csv"))
                    .sorted()
                    .findFirst();
        }
    }
}
