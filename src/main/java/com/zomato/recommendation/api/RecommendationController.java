package com.zomato.recommendation.api;

import com.zomato.recommendation.api.dto.RecommendationRequestDto;
import com.zomato.recommendation.api.dto.RecommendationResponseDto;
import com.zomato.recommendation.config.AppProperties;
import com.zomato.recommendation.domain.UserPreferences;
import com.zomato.recommendation.service.RecommendationOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationOrchestrator orchestrator;
    private final int defaultTopN;

    public RecommendationController(RecommendationOrchestrator orchestrator, AppProperties appProperties) {
        this.orchestrator = orchestrator;
        this.defaultTopN = appProperties.recommendation().topN();
    }

    @PostMapping
    public ResponseEntity<RecommendationResponseDto> getRecommendations(@Valid @RequestBody RecommendationRequestDto request) {
        UserPreferences preferences = new UserPreferences(
                request.location(),
                request.budget(),
                request.cuisine(),
                request.minRating(),
                request.additionalPreferences()
        );

        int topN = request.topN() != null ? request.topN() : defaultTopN;
        RecommendationResponseDto response = orchestrator.recommend(preferences, topN);

        return ResponseEntity.ok(response);
    }
}
