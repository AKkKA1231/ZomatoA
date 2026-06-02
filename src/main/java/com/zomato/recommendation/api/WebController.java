package com.zomato.recommendation.api;

import com.zomato.recommendation.api.dto.RecommendationResponseDto;
import com.zomato.recommendation.domain.BudgetBand;
import com.zomato.recommendation.domain.UserPreferences;
import com.zomato.recommendation.service.MetadataService;
import com.zomato.recommendation.service.RecommendationOrchestrator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
public class WebController {

    private final MetadataService metadataService;
    private final RecommendationOrchestrator orchestrator;

    public WebController(MetadataService metadataService, RecommendationOrchestrator orchestrator) {
        this.metadataService = metadataService;
        this.orchestrator = orchestrator;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        List<String> cities = metadataService.getDistinctCities();
        List<String> cuisines = metadataService.getDistinctCuisines(Optional.empty());

        model.addAttribute("cities", cities);
        model.addAttribute("cuisines", cuisines);
        model.addAttribute("budgetBands", BudgetBand.values());
        return "index";
    }

    @PostMapping("/recommendations")
    public String getRecommendations(
            @RequestParam("city") String city,
            @RequestParam("cuisine") String cuisine,
            @RequestParam("budget") BudgetBand budget,
            @RequestParam("minRating") BigDecimal minRating,
            @RequestParam(value = "additionalPreferences", required = false) String additionalPreferences,
            @RequestParam(value = "topN", defaultValue = "5") int topN,
            Model model) {

        UserPreferences preferences = new UserPreferences(
                city,
                budget,
                cuisine,
                minRating,
                additionalPreferences
        );

        RecommendationResponseDto response = orchestrator.recommend(preferences, topN);

        model.addAttribute("response", response);
        model.addAttribute("preferences", preferences);
        model.addAttribute("topN", topN);
        return "results";
    }
}
