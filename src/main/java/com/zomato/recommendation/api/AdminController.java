package com.zomato.recommendation.api;

import com.zomato.recommendation.api.dto.IngestResponseDto;
import com.zomato.recommendation.domain.IngestSummary;
import com.zomato.recommendation.service.DatasetIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/admin")
// @Profile("dev")
public class AdminController {

    private final DatasetIngestionService ingestionService;

    public AdminController(DatasetIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<IngestResponseDto> ingest() throws IOException {
        IngestSummary summary = ingestionService.ingest();
        return ResponseEntity.ok(IngestResponseDto.from(summary));
    }
}
