package com.zomato.recommendation.infrastructure.llm;

/**
 * Custom exception representing failures during LLM invocation, parsing, or validation.
 */
public class LlmException extends RuntimeException {
    public LlmException(String message) {
        super(message);
    }

    public LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
