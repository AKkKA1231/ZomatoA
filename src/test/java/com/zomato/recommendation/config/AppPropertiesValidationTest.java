package com.zomato.recommendation.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;

class AppPropertiesValidationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues(
                    "app.data.cache-path=data/processed/restaurants.json",
                    "app.data.dataset-id=ManikaSaini/zomato-restaurant-recommendation",
                    "app.data.raw-path=data/raw",
                    "app.data.auto-ingest-on-startup=false",
                    "app.recommendation.shortlist-max=20",
                    "app.recommendation.pre-rank.rating-weight=0.5",
                    "app.recommendation.pre-rank.cost-weight=0.3",
                    "app.recommendation.pre-rank.cuisine-weight=0.2",
                    "app.llm.provider=groq",
                    "app.llm.base-url=https://api.groq.com/openai/v1",
                    "app.llm.model=llama3-8b-8192",
                    "app.llm.temperature=0.2",
                    "app.llm.timeout=30s",
                    "app.llm.max-retries=2"
            );

    @Test
    void rejectsNegativeTopN() {
        contextRunner
                .withPropertyValues("app.recommendation.top-n=-1")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void acceptsValidTopN() {
        contextRunner
                .withPropertyValues("app.recommendation.top-n=5")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(AppProperties.class).recommendation().topN()).isEqualTo(5);
                });
    }

    @Configuration
    @EnableConfigurationProperties(AppProperties.class)
    static class TestConfig {
        MethodValidationPostProcessor methodValidationPostProcessor() {
            return new MethodValidationPostProcessor();
        }
    }
}
