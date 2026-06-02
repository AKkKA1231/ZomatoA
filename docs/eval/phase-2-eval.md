# Phase 2 Evaluation: Retrieval Layer

**Phase goal:** Filter, pre-rank, cap shortlist; metadata APIs; empty-result suggestions.  
**Plan reference:** [implementationPlan.md § Phase 2](../implementationPlan.md#phase-2-retrieval-layer)  
**Architecture reference:** [architecture.md §3.3, §5.1](../architecture.md)

---

## Entry criteria

- [ ] [Phase 1 eval](./phase-1-eval.md) signed off
- [ ] Repository loaded with fixture or full data

---

## Must-pass criteria

### FilterService

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E2.1 | City filter works | Known city returns subset; unknown city returns empty | ☐ |
| E2.2 | Cuisine filter works | Matching cuisine included; case-insensitive | ☐ |
| E2.3 | Min rating filter works | All results `rating >= minRating` | ☐ |
| E2.4 | Budget band filter works | All results match requested `BudgetBand` | ☐ |
| E2.5 | Combined filters (AND) | All predicates apply together | ☐ |
| E2.6 | Shortlist capped | Result size ≤ `app.recommendation.shortlist-max` | ☐ |
| E2.7 | Pre-rank deterministic | Same input twice → same order | ☐ |
| E2.8 | Empty result includes suggestions | ≥2 actionable strings (e.g. lower rating, change budget) | ☐ |

### Performance (informal)

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E2.9 | Filter latency on full data | Single filter call &lt; 150 ms locally | ☐ |

### Metadata APIs

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E2.10 | `GET /api/v1/metadata/cities` | 200, non-empty JSON array on loaded data | ☐ |
| E2.11 | `GET /api/v1/metadata/cuisines` | 200, non-empty list | ☐ |
| E2.12 | `GET /api/v1/metadata/cuisines?city={valid}` | Subset or scoped list; no 500 | ☐ |

### Testing

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E2.13 | Unit tests per filter dimension | Each filter tested in isolation | ☐ |
| E2.14 | Unit test: pre-rank ordering | Higher score rows first | ☐ |
| E2.15 | Integration test with fixture | Known preferences → expected shortlist size | ☐ |
| E2.16 | MockMvc metadata tests | Cities/cuisines endpoints return 200 | ☐ |

---

## Should-pass criteria

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E2.17 | Realistic combo on full data | e.g. Bangalore + Italian + MEDIUM + 4.0 → non-empty | ☐ |
| E2.18 | Impossible combo | Returns empty + suggestions | ☐ |
| E2.19 | Free-text stored on `UserPreferences` | Field populated for Phase 3 prompt | ☐ |

---

## Edge cases to verify

From [edgecase.md](../edgecase.md): **F-01**–**F-08**, **F-11**, **B-02**, **M-01**–**M-02**

| ID | Scenario | Expected | Pass |
|----|----------|----------|------|
| F-01 | City typo | Empty + suggestions | ☐ |
| F-05 | Broad filters (many matches) | Capped to shortlist_max | ☐ |
| F-06 | Impossible combo | Empty, no exception | ☐ |
| F-07 | Exactly 1 match | Shortlist size 1 | ☐ |
| F-11 | Whitespace location | 400 when via API (Phase 4) or rejected in service | ☐ |
| B-02 | Budget with no rows in city | Empty + suggestions | ☐ |

---

## Sample API checks

```bash
curl -s http://localhost:8080/api/v1/metadata/cities | head
curl -s "http://localhost:8080/api/v1/metadata/cuisines?city=Bangalore" | head
```

---

## Automated test gate

```bash
mvn -q clean test -Dtest="*Filter*,*Metadata*,*PreRank*"
```

---

## Sign-off

| Role | Name | Date | Approved |
|------|------|------|----------|
| Developer | | | ☐ |

**Phase 2 complete when:** All **must-pass** (E2.1–E2.16) are checked.

**Next phase:** [phase-3-eval.md](./phase-3-eval.md)
