# Phase 1 Evaluation: Data Pipeline

**Phase goal:** Ingest, normalize, cache, and load restaurants into `RestaurantRepository`.  
**Plan reference:** [implementationPlan.md § Phase 1](../implementationPlan.md#phase-1-data-pipeline)  
**Architecture reference:** [architecture.md §3.1, §3.2, §4.1](../architecture.md)

---

## Entry criteria

- [ ] [Phase 0 eval](./phase-0-eval.md) signed off
- [ ] Raw dataset available (Hugging Face download or `data/raw/` CSV)

---

## Must-pass criteria

### Domain & repository

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E1.1 | `Restaurant` model complete | id, name, city, location, cuisines, rating, costForTwo, budgetBand | ☐ |
| E1.2 | `RestaurantRepository.findById` works | Known id from fixture returns `Optional` present | ☐ |
| E1.3 | Repository count matches cache | Count after load equals ingest metadata `rowCount` | ☐ |
| E1.4 | Data loads on startup | Logs confirm load; no lazy failure on first query | ☐ |

### Ingestion

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E1.5 | Ingest produces processed cache | `data/processed/restaurants.json` (or CSV) exists after ingest | ☐ |
| E1.6 | `metadata.json` written | Contains ingest timestamp, row count, band thresholds | ☐ |
| E1.7 | `restaurantId` stable | Re-ingest same raw data → same ids for same name+city | ☐ |
| E1.8 | Budget bands assigned | Every row with valid cost has LOW/MEDIUM/HIGH | ☐ |
| E1.9 | Invalid rows skipped | Ingest summary logs skip count; no crash | ☐ |
| E1.10 | `data/schema.json` documented | Column mapping recorded after first successful ingest | ☐ |

### Admin / dev workflow

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E1.11 | Dev ingest trigger works | `POST /api/v1/admin/ingest` (dev profile) or CLI flag completes | ☐ |

### Testing

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E1.12 | CI uses fixture only | `mvn test` passes without HF download | ☐ |
| E1.13 | Unit tests: `BudgetBandCalculator` | Percentile edge cases covered | ☐ |
| E1.14 | Unit/integration: ingest on fixture | ~100 row fixture loads correctly | ☐ |

---

## Should-pass criteria

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E1.15 | Full dataset ingest (local) | 50k+ rows ingested once; startup &lt; 30s | ☐ |
| E1.16 | Cuisine list parsed | Multi-cuisine field → `List<String>` size &gt; 1 where applicable | ☐ |
| E1.17 | Health reflects data (if indicator exists early) | Custom health UP when data loaded | ☐ |

---

## Edge cases to verify

From [edgecase.md](../edgecase.md): **D-02** through **D-10**, **D-13**, **B-01**

| ID | Scenario | Expected | Pass |
|----|----------|----------|------|
| D-02 | Delete cache, restart | Health DOWN or clear error; no fake recommendations | ☐ |
| D-03 | Truncate cache file manually | Loader fails safely | ☐ |
| D-06 | Fixture row missing name | Skipped in ingest test | ☐ |
| D-07 | Non-numeric rating in raw | Skipped or null handling | ☐ |
| D-13 | `"Italian, Chinese"` in raw | Both cuisines in model | ☐ |
| B-01 | All same cost in tiny fixture | Bands assigned without exception | ☐ |

---

## Automated test gate

```bash
mvn -q clean test
```

No test should download the full 574 MB dataset.

---

## Manual verification script

```bash
# ingest (dev)
curl -X POST http://localhost:8080/api/v1/admin/ingest

# verify count via logs or temporary debug endpoint
# restart app and confirm repository size in logs
```

---

## Sign-off

| Role | Name | Date | Approved |
|------|------|------|----------|
| Developer | | | ☐ |

**Phase 1 complete when:** All **must-pass** (E1.1–E1.14) are checked.

**Next phase:** [phase-2-eval.md](./phase-2-eval.md)
