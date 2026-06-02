package com.zomato.recommendation.infrastructure.dataset;

import com.zomato.recommendation.service.RestaurantIdGenerator;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RawRestaurantParserTest {

    private final RawRestaurantParser parser = new RawRestaurantParser(new RestaurantIdGenerator());

    @Test
    void skipsRowWithMissingRating() {
        Map<String, String> headerMap = Map.of(
                ColumnMapping.FIELD_NAME, "Restaurant Name",
                ColumnMapping.FIELD_CITY, "City",
                ColumnMapping.FIELD_CUISINES, "Cuisines",
                ColumnMapping.FIELD_RATING, "Aggregate rating",
                ColumnMapping.FIELD_COST, "Average Cost for two people",
                ColumnMapping.FIELD_LOCATION, "Address",
                ColumnMapping.FIELD_VOTES, "Votes"
        );

        Map<String, String> invalid = row(
                "Invalid",
                "Chennai",
                "Indian",
                "",
                "600",
                "Addr",
                "10"
        );

        var result = parser.parseRows(List.of(invalid), headerMap);
        assertThat(result.restaurants()).isEmpty();
        assertThat(result.skipped()).isOne();
    }

    @Test
    void parsesMultiCuisine() {
        Map<String, String> headerMap = Map.of(
                ColumnMapping.FIELD_NAME, "Restaurant Name",
                ColumnMapping.FIELD_CITY, "City",
                ColumnMapping.FIELD_CUISINES, "Cuisines",
                ColumnMapping.FIELD_RATING, "Aggregate rating",
                ColumnMapping.FIELD_COST, "Average Cost for two people",
                ColumnMapping.FIELD_LOCATION, "Address",
                ColumnMapping.FIELD_VOTES, "Votes"
        );

        Map<String, String> valid = row(
                "Trattoria",
                "Bangalore",
                "Italian, Chinese",
                "4.5",
                "1500",
                "Indiranagar",
                "100"
        );

        var result = parser.parseRows(List.of(valid), headerMap);
        assertThat(result.restaurants()).hasSize(1);
        assertThat(result.restaurants().get(0).cuisines()).containsExactly("Italian", "Chinese");
    }

    private static Map<String, String> row(String name, String city, String cuisines, String rating,
                                           String cost, String address, String votes) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("Restaurant Name", name);
        row.put("City", city);
        row.put("Cuisines", cuisines);
        row.put("Aggregate rating", rating);
        row.put("Average Cost for two people", cost);
        row.put("Address", address);
        row.put("Votes", votes);
        return row;
    }
}
