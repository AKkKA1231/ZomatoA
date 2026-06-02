package com.zomato.recommendation.api;

import com.zomato.recommendation.service.MetadataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/metadata")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/cities")
    public List<String> getCities() {
        return metadataService.getDistinctCities();
    }

    @GetMapping("/cuisines")
    public List<String> getCuisines(@RequestParam(required = false) String city) {
        return metadataService.getDistinctCuisines(Optional.ofNullable(city));
    }
}
