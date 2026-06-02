package com.zomato.recommendation.config;

import com.zomato.recommendation.infrastructure.llm.LlmGateway;
import com.zomato.recommendation.infrastructure.llm.LlmResponseParser;
import com.zomato.recommendation.infrastructure.llm.WebClientLlmGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LlmGatewayConfig {

    @Bean
    public LlmGateway llmGateway(WebClient.Builder webClientBuilder, AppProperties appProperties, LlmResponseParser parser) {
        return new WebClientLlmGateway(webClientBuilder, appProperties, parser);
    }
}
