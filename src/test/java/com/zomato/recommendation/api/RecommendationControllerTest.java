package com.zomato.recommendation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zomato.recommendation.api.dto.RecommendationRequestDto;
import com.zomato.recommendation.api.dto.RecommendationResponseDto;
import com.zomato.recommendation.domain.BudgetBand;
import com.zomato.recommendation.service.RecommendationOrchestrator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zomato.recommendation.config.AppProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@WebMvcTest(RecommendationController.class)
@EnableConfigurationProperties(AppProperties.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecommendationOrchestrator orchestrator;

    @Test
    void shouldReturnRecommendationsOnValidRequest() throws Exception {
        RecommendationRequestDto requestDto = new RecommendationRequestDto(
                "Bangalore",
                BudgetBand.MEDIUM,
                "Italian",
                BigDecimal.valueOf(4.0),
                "No preferences",
                3
        );

        RecommendationResponseDto responseDto = new RecommendationResponseDto(
                List.of(new RecommendationResponseDto.RecommendationItemDto(
                        "1", "Toscano", List.of("Italian"), BigDecimal.valueOf(4.2), 1500, "Nice explanation"
                )),
                "Curated options",
                List.of(),
                new RecommendationResponseDto.MetaDto(1, false)
        );

        when(orchestrator.recommend(any(), anyInt())).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Curated options"))
                .andExpect(jsonPath("$.recommendations[0].name").value("Toscano"))
                .andExpect(jsonPath("$.meta.degraded").value(false));
    }

    @Test
    void shouldReturn400OnInvalidRequest() throws Exception {
        RecommendationRequestDto invalidRequest = new RecommendationRequestDto(
                "", // Blank location
                null, // Null budget
                "Italian",
                BigDecimal.valueOf(6.0), // Invalid rating (> 5.0)
                "None",
                15 // Invalid topN (> 10)
        );

        mockMvc.perform(post("/api/v1/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
