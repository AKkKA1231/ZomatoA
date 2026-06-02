package com.zomato.recommendation.infrastructure.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zomato.recommendation.domain.RecommendationResponse;
import org.springframework.stereotype.Component;

@Component
public class LlmResponseParser {

    private final ObjectMapper objectMapper;

    public LlmResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RecommendationResponse parse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new LlmException("Empty response received from LLM");
        }

        String cleaned = cleanFences(rawResponse);

        try {
            return objectMapper.readValue(cleaned, RecommendationResponse.class);
        } catch (Exception e) {
            throw new LlmException("Failed to parse LLM JSON output. Raw response was: " + rawResponse, e);
        }
    }

    private String cleanFences(String input) {
        String trimmed = input.trim();
        // Remove leading ```json or ```
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            } else {
                trimmed = trimmed.substring(3);
            }
            // Remove trailing ```
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.trim();
    }
}
