package com.zomato.recommendation.infrastructure.dataset;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Maps heterogeneous Zomato CSV headers to canonical field names.
 */
public final class ColumnMapping {

    public static final String FIELD_NAME = "name";
    public static final String FIELD_CITY = "listed_in(city)";
    public static final String FIELD_CUISINES = "cuisines";
    public static final String FIELD_RATING = "rating";
    public static final String FIELD_COST = "costForTwo";
    public static final String FIELD_LOCATION = "location";
    public static final String FIELD_VOTES = "votes";

    private static final Map<String, List<String>> ALIASES = Map.of(
            FIELD_NAME, List.of("restaurant name", "name", "restaurant_name", "restaurant"),
            FIELD_CITY, List.of("city", "city name", "listed_city"),
            FIELD_CUISINES, List.of("cuisines", "cuisine"),
            FIELD_RATING, List.of("aggregate rating", "rating", "aggregate_rating", "rate"),
            FIELD_COST, List.of(
                    "average cost for two people",
                    "average_cost_for_two",
                    "cost_for_two",
                    "approx_cost(for two people)",
                    "average cost"
            ),
            FIELD_LOCATION, List.of("address", "location", "listed_address"),
            FIELD_VOTES, List.of("votes", "rating_count")
    );

    private ColumnMapping() {
    }

    public static Map<String, String> resolveHeaderMap(List<String> headers) {
        Map<String, String> resolved = new java.util.HashMap<>();
        for (String header : headers) {
            canonicalField(header).ifPresent(canonical -> resolved.putIfAbsent(canonical, header));
        }
        return resolved;
    }

    public static Optional<String> canonicalField(String header) {
        if (header == null || header.isBlank()) {
            return Optional.empty();
        }
        String normalized = header.trim().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, List<String>> entry : ALIASES.entrySet()) {
            for (String alias : entry.getValue()) {
                if (normalized.equals(alias)) {
                    return Optional.of(entry.getKey());
                }
            }
        }
        return Optional.empty();
    }

    public static String get(Map<String, String> row, Map<String, String> headerMap, String field) {
        String header = headerMap.get(field);
        if (header == null) {
            return null;
        }
        return row.get(header);
    }
}
