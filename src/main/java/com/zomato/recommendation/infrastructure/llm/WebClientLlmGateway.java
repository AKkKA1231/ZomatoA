package com.zomato.recommendation.infrastructure.llm;

import com.zomato.recommendation.config.AppProperties;
import com.zomato.recommendation.domain.RecommendationResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

/**
 * WebClient-based implementation of LlmGateway that supports standard OpenAI chat completion API.
 */
public class WebClientLlmGateway implements LlmGateway {

    private final WebClient webClient;
    private final AppProperties.LlmProperties properties;
    private final LlmResponseParser parser;

    public WebClientLlmGateway(WebClient.Builder webClientBuilder, AppProperties appProperties, LlmResponseParser parser) {
        this.properties = appProperties.llm();
        this.parser = parser;
        this.webClient = webClientBuilder
                .baseUrl(properties.baseUrl())
                .build();
    }

    @Override
    public RecommendationResponse complete(String prompt) {
        String apiKey = properties.apiKey();
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.startsWith("${") || apiKey.equals("None")) {
            throw new LlmException("API Key for LLM provider is not configured. Please set the appropriate environment variable.");
        }

        ChatRequest request = new ChatRequest(
                properties.model(),
                List.of(new Message("user", prompt)),
                properties.temperature()
        );

        try {
            ChatResponse chatResponse = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .timeout(properties.timeout())
                    .retryWhen(Retry.backoff(properties.maxRetries(), Duration.ofMillis(100))
                            .filter(throwable -> {
                                if (throwable instanceof WebClientResponseException wcre) {
                                    return wcre.getStatusCode().is5xxServerError() || wcre.getStatusCode().value() == 429;
                                }
                                return false;
                            })
                    )
                    .block();

            if (chatResponse == null || chatResponse.choices() == null || chatResponse.choices().isEmpty()) {
                throw new LlmException("Invalid or empty response structure from LLM");
            }

            String content = chatResponse.choices().get(0).message().content();
            return parser.parse(content);
        } catch (WebClientResponseException e) {
            throw new LlmException("LLM provider returned error status " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null && cause != cause.getCause()) {
                cause = cause.getCause();
            }
            if (cause instanceof LlmException) {
                throw (LlmException) cause;
            }
            if (cause instanceof WebClientResponseException wcre) {
                throw new LlmException("LLM provider returned error status " + wcre.getStatusCode() + ": " + wcre.getResponseBodyAsString(), wcre);
            }
            throw new LlmException("Failed to invoke LLM provider: " + cause.getMessage(), e);
        }
    }

    private record ChatRequest(String model, List<Message> messages, double temperature) {}
    private record Message(String role, String content) {}
    private record ChatResponse(List<Choice> choices) {}
    private record Choice(Message message) {}
}
