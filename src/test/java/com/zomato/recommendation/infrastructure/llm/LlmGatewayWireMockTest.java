package com.zomato.recommendation.infrastructure.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.zomato.recommendation.config.AppProperties;
import com.zomato.recommendation.domain.RecommendationResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LlmGatewayWireMockTest {

    private WireMockServer wireMockServer;
    private WebClientLlmGateway llmGateway;
    private AppProperties.LlmProperties llmProperties;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0); // dynamic port
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());

        llmProperties = new AppProperties.LlmProperties(
                "groq",
                "http://localhost:" + wireMockServer.port(),
                "llama3-8b-8192",
                "test-api-key",
                0.2,
                Duration.ofSeconds(2), // 2 seconds timeout for testing
                2 // Max retries
        );

        AppProperties appProperties = new AppProperties(
                new AppProperties.DataProperties("data/processed/restaurants.json", "dataset", "data/raw", false),
                new AppProperties.RecommendationProperties(5, 20, new AppProperties.PreRankProperties(0.5, 0.3, 0.2)),
                llmProperties
        );

        LlmResponseParser parser = new LlmResponseParser(new ObjectMapper());
        llmGateway = new WebClientLlmGateway(WebClient.builder(), appProperties, parser);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void shouldCompleteSuccessfullyOnValidApiResponse() {
        String mockResponse = """
                {
                  "choices": [
                    {
                      "message": {
                        "role": "assistant",
                        "content": "{\\"recommendations\\":[],\\"summary\\":\\"No matching items\\"}"
                      }
                    }
                  ]
                }
                """;

        stubFor(post(urlEqualTo("/chat/completions"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(mockResponse)));

        RecommendationResponse response = llmGateway.complete("Recommend restaurants");

        assertThat(response).isNotNull();
        assertThat(response.summary()).isEqualTo("No matching items");
        assertThat(response.recommendations()).isEmpty();
    }

    @Test
    void shouldFailOnTimeout() {
        stubFor(post(urlEqualTo("/chat/completions"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withFixedDelay(3000) // Delay is longer than 2s timeout
                        .withBody("{}")));

        assertThatThrownBy(() -> llmGateway.complete("Recommend restaurants"))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("Failed to invoke LLM provider");
    }

    @Test
    void shouldRetryOnTransientServerErrorAndSucceed() {
        String mockResponse = """
                {
                  "choices": [
                    {
                      "message": {
                        "role": "assistant",
                        "content": "{\\"recommendations\\":[],\\"summary\\":\\"Retry success\\"}"
                      }
                    }
                  ]
                }
                """;

        // First call: 503 Server Error
        stubFor(post(urlEqualTo("/chat/completions"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("First Failure"));

        // Second call: 200 Success
        stubFor(post(urlEqualTo("/chat/completions"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("First Failure")
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(mockResponse)));

        RecommendationResponse response = llmGateway.complete("Recommend restaurants");

        assertThat(response).isNotNull();
        assertThat(response.summary()).isEqualTo("Retry success");
    }
}
