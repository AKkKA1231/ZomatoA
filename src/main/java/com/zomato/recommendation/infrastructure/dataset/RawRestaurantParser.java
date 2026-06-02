package com.zomato.recommendation.infrastructure.dataset;

import com.zomato.recommendation.domain.Restaurant;
import com.zomato.recommendation.service.RestaurantIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RawRestaurantParser {

    private static final Logger log = LoggerFactory.getLogger(RawRestaurantParser.class);

    private final RestaurantIdGenerator idGenerator;

    public RawRestaurantParser(RestaurantIdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public ParseResult parseRows(List<Map<String, String>> rows, Map<String, String> headerMap) {
        List<Restaurant> restaurants = new ArrayList<>();
        int skipped = 0;

        for (Map<String, String> row : rows) {
            Optional<Restaurant> parsed = parseRow(row, headerMap);
            if (parsed.isPresent()) {
                restaurants.add(parsed.get());
            } else {
                skipped++;
            }
        }

        if (skipped > 0) {
            log.info("Skipped {} invalid or incomplete rows during parse", skipped);
        }

        return new ParseResult(restaurants, skipped);
    }

    public Optional<Restaurant> parseRow(Map<String, String> row, Map<String, String> headerMap) {
        String name = trim(ColumnMapping.get(row, headerMap, ColumnMapping.FIELD_NAME));
        String city = trim(ColumnMapping.get(row, headerMap, ColumnMapping.FIELD_CITY));

        if (name == null || name.isBlank() || city == null || city.isBlank()) {
            return Optional.empty();
        }

        String location = defaultString(trim(ColumnMapping.get(row, headerMap, ColumnMapping.FIELD_LOCATION)), city);
        List<String> cuisines = parseCuisines(ColumnMapping.get(row, headerMap, ColumnMapping.FIELD_CUISINES));
        BigDecimal rating = parseRating(ColumnMapping.get(row, headerMap, ColumnMapping.FIELD_RATING));
        Integer cost = parseCost(ColumnMapping.get(row, headerMap, ColumnMapping.FIELD_COST));
        Integer votes = parseVotes(ColumnMapping.get(row, headerMap, ColumnMapping.FIELD_VOTES));

        if (rating == null) {
            return Optional.empty();
        }

        String id = idGenerator.generate(name, city);

        return Optional.of(new Restaurant(
                id,
                name,
                normalizeCity(city),
                location,
                cuisines,
                rating,
                cost,
                null,
                votes
        ));
    }

    public record ParseResult(List<Restaurant> restaurants, int skipped) {
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String normalizeCity(String city) {
        return city.trim();
    }

    private static List<String> parseCuisines(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of("Unknown");
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private static BigDecimal parseRating(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            String cleaned = raw.replaceAll("[^0-9.]", "");
            if (cleaned.isBlank()) {
                return null;
            }
            BigDecimal rating = new BigDecimal(cleaned);
            if (rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(new BigDecimal("5")) > 0) {
                return null;
            }
            return rating;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseCost(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            String digits = raw.replaceAll("[^0-9]", "");
            if (digits.isBlank()) {
                return null;
            }
            int cost = Integer.parseInt(digits);
            return cost > 0 ? cost : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseVotes(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            String digits = raw.replaceAll("[^0-9]", "");
            return digits.isBlank() ? null : Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
