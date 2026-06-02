package com.zomato.recommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zomato.recommendation.domain.Restaurant;
import com.zomato.recommendation.domain.UserPreferences;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class PromptBuilder {

    private final ObjectMapper objectMapper;
    private final String template;

    public PromptBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.template = loadTemplate();
    }

    private String loadTemplate() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("prompts/recommend-v1.txt")) {
            if (is == null) {
                throw new IllegalStateException("Prompt template file prompts/recommend-v1.txt not found on classpath");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load prompt template", e);
        }
    }

    public String build(UserPreferences preferences, List<Restaurant> candidates, int topN) {
        try {
            String candidatesJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(candidates);

            return template
                    .replace("{{topN}}", String.valueOf(topN))
                    .replace("{{city}}", preferences.city() != null ? preferences.city() : "ANY")
                    .replace("{{budget}}", preferences.budget() != null ? preferences.budget().name() : "ANY")
                    .replace("{{cuisine}}", preferences.cuisine() != null ? preferences.cuisine() : "ANY")
                    .replace("{{minRating}}", preferences.minRating() != null ? preferences.minRating().toString() : "ANY")
                    .replace("{{additionalPreferences}}", (preferences.additionalPreferences() != null && !preferences.additionalPreferences().isBlank()) 
                            ? preferences.additionalPreferences() : "None")
                    .replace("{{candidatesJson}}", candidatesJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build prompt", e);
        }
    }
}
