package com.zomato.recommendation.api;

import com.zomato.recommendation.service.MetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetadataController.class)
class MetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetadataService metadataService;

    @Test
    void shouldReturnCities() throws Exception {
        when(metadataService.getDistinctCities()).thenReturn(List.of("Bangalore", "Delhi"));

        mockMvc.perform(get("/api/v1/metadata/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("Bangalore"))
                .andExpect(jsonPath("$[1]").value("Delhi"));
    }

    @Test
    void shouldReturnCuisines() throws Exception {
        when(metadataService.getDistinctCuisines(Optional.empty())).thenReturn(List.of("Italian", "Chinese"));

        mockMvc.perform(get("/api/v1/metadata/cuisines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("Italian"))
                .andExpect(jsonPath("$[1]").value("Chinese"));
    }

    @Test
    void shouldReturnCuisinesForCity() throws Exception {
        when(metadataService.getDistinctCuisines(Optional.of("Bangalore"))).thenReturn(List.of("Italian"));

        mockMvc.perform(get("/api/v1/metadata/cuisines").param("city", "Bangalore"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0]").value("Italian"));
    }
}
