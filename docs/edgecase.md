# Edge Cases & Failure Scenarios

Catalog of boundary conditions, invalid inputs, and failure modes for the ZOMATO recommendation system. Use alongside [architecture.md](./architecture.md) and [implementationPlan.md](./implementationPlan.md). Per-phase verification lives in [eval/](./eval/).

**Legend**

| Severity | Meaning |
|----------|---------|
| **Critical** | Wrong data, hallucinated restaurants, or crash |
| **High** | Bad UX, empty/error without guidance |
| **Medium** | Degraded but recoverable |
| **Low** | Cosmetic or rare; document behavior |

| Phase | Primary owner of fix |
|-------|----------------------|
| P0–P1 | Data / bootstrap |
| P2 | Filter / metadata |
| P3 | LLM client / parser |
| P4 | Orchestrator / API |
| P5 | Errors / ops |
| P6 | UI |

---

## 1. Data ingestion & cache (Phase 1)

| ID | Edge case | Expected behavior | Severity | Phase |
|----|-----------|-------------------|----------|-------|
| D-01 | Hugging Face download fails (network, 403, timeout) | Ingest fails with clear error; app may start if existing cache present; log cause | High | P1 |
| D-02 | Cache file missing on startup | Fail health check OR trigger dev-only auto-ingest per config; never silent empty corpus | Critical | P1, P5 |
| D-03 | Corrupt / partial JSON cache (truncated file) | Loader rejects file; log path; health DOWN; do not serve partial data as complete | Critical | P1 |
| D-04 | Empty cache file (`[]`) | Health DOWN; recommend returns actionable “data not loaded” | Critical | P1, P4 |
| D-05 | Unknown / renamed CSV columns from dataset | Map via config; unmapped required columns → skip row + warn; document in `schema.json` | High | P1 |
| D-06 | Row missing required fields (name, city) | Skip row; increment skip counter in ingest summary | Medium | P1 |
| D-07 | Non-numeric rating or cost | Skip row or set null; exclude from rating/cost filters if null | Medium | P1 |
| D-08 | Rating outside 0–5 | Clamp or skip per config; validation on API still 0–5 | Medium | P1, P4 |
| D-09 | Negative or zero `cost_for_two` | Treat as null or skip; exclude from budget band or assign LOW | Medium | P1 |
| D-10 | Duplicate restaurant name + city | Stable `restaurantId` via hash; last-write-wins or merge with log | Medium | P1 |
| D-11 | Extremely long name / address / cuisine string | Truncate for LLM prompt; full value in repository/display | Low | P1, P3 |
| D-12 | City name variants (`Bengaluru` vs `Bangalore`) | Document canonical names in metadata; case-insensitive match; alias table optional | High | P1, P2 |
| D-13 | Cuisine string `"Italian, Chinese, Fast Food"` | Split to list; match if any token matches user cuisine | Medium | P1, P2 |
| D-14 | All rows in city have null cost | Budget band defaults; filter by band may return empty → suggestions | High | P1, P2 |
| D-15 | Single-city dataset slice (only one city) | Metadata lists one city; other cities return empty filter | Medium | P2 |
| D-16 | Re-ingest while app running | Reload repository atomically or require restart; document behavior | Medium | P1 |
| D-17 | Disk full during cache write | Ingest fails; keep previous cache if exists | High | P1 |
| D-18 | File encoding not UTF-8 | Configure UTF-8 reader; replace invalid chars or fail ingest | Medium | P1 |
| D-19 | 51k+ rows memory pressure | Monitor heap; document minimum RAM; optional future DB | Medium | P1 |
| D-20 | Manual `data/raw/` CSV with wrong delimiter | Parser error with line number; no partial silent load | Medium | P1 |

---

## 2. Budget bands (Phase 1–2)

| ID | Edge case | Expected behavior | Severity | Phase |
|----|-----------|-------------------|----------|-------|
| B-01 | All restaurants same cost | All bands equal or single band; filter still works | Medium | P1 |
| B-02 | User budget MEDIUM but no MEDIUM-band rows in city | Empty filter → suggestions (lower rating, other budget) | High | P2 |
| B-03 | Cost null but rating high | Include in rating filter; budget filter excludes or uses default band | Medium | P2 |
| B-04 | Per-city vs global percentile config switch | Bands recomputed on ingest; metadata stores thresholds | Low | P1 |

---

## 3. Filtering & pre-rank (Phase 2)

| ID | Edge case | Expected behavior | Severity | Phase |
|----|-----------|-------------------|----------|-------|
| F-01 | City not in dataset (typo, wrong case) | Empty shortlist + suggestion to pick from `/metadata/cities` | High | P2, P4 |
| F-02 | Cuisine not in dataset | Empty or partial match per substring rules; suggestions | High | P2 |
| F-03 | `min_rating` = 5.0 with no 5.0 restaurants | Empty + suggest lower rating | High | P2 |
| F-04 | `min_rating` = 0 | No rating filter effectively; large shortlist capped by pre-rank | Medium | P2 |
| F-05 | Filters match 10,000+ rows | Pre-rank + cap to `shortlist_max` before LLM | Critical | P2 |
| F-06 | Filters match exactly 0 rows | No LLM call; ≥2 suggestions | Critical | P2, P4 |
| F-07 | Filters match 1 row | Shortlist of 1; LLM returns 1 rec or fallback 1 | Medium | P2, P4 |
| F-08 | Filters match fewer than `top_n` | Return all matched; LLM/fallback returns ≤ available | Medium | P4 |
| F-09 | Cuisine substring false positive (`Art` matches `Italian`) | Document matching rules; prefer word-boundary if implemented | Low | P2 |
| F-10 | Special characters in cuisine search (`North Indian`, `Café`) | Unicode-safe case-insensitive match | Medium | P2 |
| F-11 | Whitespace-only location/cuisine in API | 400 validation error | High | P4, P5 |
| F-12 | `shortlist_max` > token budget for LLM | Prompt builder must cap; config validation at startup | High | P3 |
| F-13 | Pre-rank tie scores | Stable secondary sort (e.g. rating desc, then name) | Low | P2 |
| F-14 | Additional preferences (free text) ignored by filter | Passed only to LLM prompt; document in API | Medium | P2, P3 |

---

## 4. Metadata APIs (Phase 2)

| ID | Edge case | Expected behavior | Severity | Phase |
|----|-----------|-------------------|----------|-------|
| M-01 | `GET /metadata/cities` before data loaded | 503 or empty list + health DOWN | High | P2, P5 |
| M-02 | `GET /metadata/cuisines?city=Unknown` | Empty list or 404 per API contract | Medium | P2 |
| M-03 | Missing `city` query on cuisines | Return global cuisines or 400 — document choice | Low | P2 |
| M-04 | Very large cuisine list (500+) | Paginate or cache response (`@Cacheable`) | Low | P2, P5 |

---

## 5. LLM integration (Phase 3–4)

| ID | Edge case | Expected behavior | Severity | Phase |
|----|-----------|-------------------|----------|-------|
| L-01 | Missing `OPENAI_API_KEY` | 503 `LLM_UNAVAILABLE` or fail fast at startup per config | High | P3, P5 |
| L-02 | Invalid / revoked API key | 503 after provider 401; log without leaking key | High | P3 |
| L-03 | Provider timeout | Retry then 503 or fallback with `degraded: true` | High | P3, P4 |
| L-04 | Provider rate limit (429) | Backoff retry; then fallback or 503 | High | P3, P4 |
| L-05 | Provider 5xx | Retry; fallback if exhausted | High | P3, P4 |
| L-06 | Response not JSON | Parser throws `LlmException`; retry once; fallback | Critical | P3, P4 |
| L-07 | JSON wrapped in markdown fences | Strip fences before parse | Medium | P3 |
| L-08 | JSON with trailing commentary | Extract first valid JSON object or fail | Medium | P3 |
| L-09 | LLM returns fewer than `top_n` items | Return what is valid; pad not required | Medium | P4 |
| L-10 | LLM returns more than `top_n` | Validator truncates to `top_n` | Medium | P4 |
| L-11 | LLM invents `restaurantId` not in shortlist | Grounding fails → retry → fallback | Critical | P4 |
| L-12 | LLM duplicates same id twice | Validator rejects duplicate → retry/fallback | High | P4 |
| L-13 | LLM returns valid id but wrong explanation | Accept explanation; enrich rating/cost from DB | Low | P4 |
| L-14 | LLM returns empty `recommendations` array | Treat as failure; fallback or summary-only response | High | P4 |
| L-15 | Prompt exceeds model context window | Reduce shortlist or trim fields; log warning | High | P3 |
| L-16 | Very long `additional_preferences` (10k chars) | Truncate in prompt; optional API max length | Medium | P3, P4 |
| L-17 | Model returns non-English text | Accept if valid; document default locale | Low | P3 |
| L-18 | Concurrent recommend requests | Stateless; WebClient thread-safe; no shared mutable prompt state | Medium | P4 |
| L-19 | Switch `app.llm.provider` misconfigured | Context fails to start or bean missing — clear startup error | High | P3 |

---

## 6. Recommendation API (Phase 4–5)

| ID | Edge case | Expected behavior | Severity | Phase |
|----|-----------|-------------------|----------|-------|
| A-01 | Missing required fields in body | 400 + field errors | High | P4, P5 |
| A-02 | Invalid `budget` enum | 400 | High | P4, P5 |
| A-03 | `min_rating` > 5 or < 0 | 400 | High | P4, P5 |
| A-04 | `top_n` = 0 or negative | 400 | High | P4, P5 |
| A-05 | `top_n` > 10 (max) | 400 or clamp per validation | Medium | P4 |
| A-06 | `top_n` null | Use default from config (e.g. 5) | Medium | P4 |
| A-07 | Malformed JSON body | 400 | High | P5 |
| A-08 | Empty JSON body `{}` | 400 | High | P5 |
| A-09 | Wrong `Content-Type` | 415 or 400 | Medium | P5 |
| A-10 | HTTP GET on POST-only endpoint | 405 | Low | P5 |
| A-11 | Extremely large request body | 413 or 400 size limit | Medium | P5 |
| A-12 | SQL/script injection in free text | Pass to LLM as data only; no template injection in prompt builder | High | P3, P4 |
| A-13 | Idempotent repeat identical request | Same logical result OK; optional cache later | Low | P4 |

---

## 7. Orchestrator & fallback (Phase 4)

| ID | Edge case | Expected behavior | Severity | Phase |
|----|-----------|-------------------|----------|-------|
| O-01 | LLM fails after max retries | `FallbackRecommendationService`; `meta.degraded=true` | Critical | P4 |
| O-02 | Grounding fails after retry | Fallback rank by pre-rank score | Critical | P4 |
| O-03 | Enrich: id in LLM response not in repository | Drop item or fail single item; log error | High | P4 |
| O-04 | Partial enrich (repository stale vs cache) | Source of truth = repository at request time | Medium | P4 |
| O-05 | `candidates_considered` = 0 but LLM called | Must not happen; assert in tests | Critical | P4 |
| O-06 | Both LLM and fallback throw | 500 with traceId | High | P4, P5 |

---

## 8. Configuration & bootstrap (Phase 0–5)

| ID | Edge case | Expected behavior | Severity | Phase |
|----|-----------|-------------------|----------|-------|
| C-01 | Invalid `application.yml` (bad types) | Fail at startup with bind exception | High | P0 |
| C-02 | Negative `shortlist_max` or weights | Startup validation error | Medium | P0 |
| C-03 | Port 8080 already in use | Startup failure with clear message | Low | P0 |
| C-04 | Wrong Java version (< 21) | Maven enforcer or compile error | Medium | P0 |
| C-05 | `dev` profile ingest exposed in prod | Admin secured or profile-disabled | Critical | P5 |

---

## 9. Observability & operations (Phase 5)

| ID | Edge case | Expected behavior | Severity | Phase |
|----|-----------|-------------------|----------|-------|
| O-01 | Health check during ingest | Report OUT_OF_SERVICE or separate ingest lock | Medium | P5 |
| O-02 | Docker container without cache volume | Health DOWN; document mount requirement | High | P5 |
| O-03 | Docker without API key | Recommend returns 503 degraded path | High | P5 |
| O-04 | Log volume from 51k skip warnings | Aggregate counts; avoid per-row INFO | Low | P1, P5 |
| O-05 | Missing traceId on error response | Generate UUID per request in MDC | Medium | P5 |

---

## 10. UI (Phase 6)

| ID | Edge case | Expected behavior | Severity | Phase |
|----|-----------|-------------------|----------|-------|
| U-01 | Submit with empty required fields | Client + server validation messages | High | P6 |
| U-02 | API 503 during form submit | Error page with retry guidance | High | P6 |
| U-03 | Zero results | Show suggestions from API, not blank page | High | P6 |
| U-04 | `degraded: true` in response | Visible banner (“AI ranking unavailable”) | Medium | P6 |
| U-05 | XSS in user free-text displayed back | Escape Thymeleaf output | Critical | P6 |
| U-06 | Double form submit | Disable button or idempotent handling | Low | P6 |
| U-07 | Browser back after results | Resubmit warning or safe GET results | Low | P6 |

---

## 11. Cross-cutting security

| ID | Edge case | Expected behavior | Severity | Phase |
|----|-----------|-------------------|----------|-------|
| S-01 | API key in logs | Never log headers or `app.llm.api-key` | Critical | P3, P5 |
| S-02 | Unauthenticated admin ingest in prod | 401/403 or endpoint absent | Critical | P5 |
| S-03 | CORS wide open in production | Restrict origins if SPA added | Medium | P6+ |

---

## 12. Test data & CI

| ID | Edge case | Expected behavior | Severity | Phase |
|----|-----------|-------------------|----------|-------|
| T-01 | CI runs without HF download | Tests use `restaurants-small.json` only | High | P1+ |
| T-02 | CI without API key | WireMock / mocked `LlmGateway` | High | P3+ |
| T-03 | Flaky LLM integration test | No live LLM in CI; manual checklist only | High | P3 |

---

## How to use this document

1. **During implementation** — When building a phase, implement handling for all **Critical** and **High** cases in that phase’s column.
2. **Before phase sign-off** — Run the phase [eval](./eval/) checklist and spot-check edge cases listed for that phase.
3. **Regression** — Add automated tests for IDs marked Critical where feasible (see eval test matrices).

---

## References

- [architecture.md](./architecture.md)
- [implementationPlan.md](./implementationPlan.md)
- [eval/](./eval/) — Per-phase evaluation criteria
