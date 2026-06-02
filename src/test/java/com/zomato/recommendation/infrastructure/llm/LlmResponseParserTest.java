package com.zomato.recommendation.infrastructure.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zomato.recommendation.domain.RecommendationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LlmResponseParserTest {

    private LlmResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new LlmResponseParser(new ObjectMapper());
    }

    @Test
    void shouldParseValidJson() {
        String json = """
                {
                  "recommendations": [
                    {
                      "restaurantId": "123",
                      "name": "Bistro",
                      "cuisines": ["Italian"],
                      "rating": 4.5,
                      "costForTwo": 1200,
                      "explanation": "Perfect match"
                    }
                  ],
                  "summary": "Great options!"
                }
                """;

        RecommendationResponse response = parser.parse(json);

        assertThat(response).isNotNull();
        assertThat(response.summary()).isEqualTo("Great options!");
        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).restaurantId()).isEqualTo("123");
    }

    @Test
    void shouldParseMarkdownFencedJson() {
        String json = """
                ```json
                {
                  "recommendations": [],
                  "summary": "Fenced summary"
                }
                ```
                """;

        RecommendationResponse response = parser.parse(json);

        assertThat(response).isNotNull();
        assertThat(response.summary()).isEqualTo("Fenced summary");
        assertThat(response.recommendations()).isEmpty();
    }

    @Test
    void shouldThrowLlmExceptionOnMalformedJson() {
        String malformedJson = "{ invalid json }";

        assertThatThrownBy(() -> parser.parse(malformedJson))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("Failed to parse LLM JSON output");
    }

    @Test
    void shouldThrowLlmExceptionOnEmptyResponse() {
        assertThatThrownBy(() -> parser.parse(""))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("Empty response");
    }
}
