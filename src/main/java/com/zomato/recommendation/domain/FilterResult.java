package com.zomato.recommendation.domain;

import java.util.List;

/**
 * Result of the filtering phase, containing a bounded shortlist or suggestions if empty.
 */
public record FilterResult(
        List<Restaurant> shortlist,
        List<String> suggestions
) {
    public static FilterResult empty(List<String> suggestions) {
        return new FilterResult(List.of(), suggestions);
    }
}
