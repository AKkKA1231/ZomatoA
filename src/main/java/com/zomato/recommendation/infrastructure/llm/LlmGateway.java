package com.zomato.recommendation.infrastructure.llm;

import com.zomato.recommendation.domain.RecommendationResponse;

/**
 * Provider-agnostic LLM interface for executing prompts and returning structured recommendations.
 */
public interface LlmGateway {
    RecommendationResponse complete(String prompt);
}
