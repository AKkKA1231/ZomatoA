# Phase 4 Evaluation: End-to-End Recommendation API

**Phase goal:** Full hybrid pipeline—filter → prompt → LLM → validate → enrich—with fallback and public REST API. **This is the core MVP gate.**  
**Plan reference:** [implementationPlan.md § Phase 4](../implementationPlan.md#phase-4-end-to-end-recommendation-api)  
**Architecture reference:** [architecture.md §3.6, §3.7, §5, §9](../architecture.md)

---

## Entry criteria

- [ ] [Phase 2 eval](./phase-2-eval.md) signed off
- [ ] [Phase 3 eval](./phase-3-eval.md) signed off
- [ ] `OPENAI_API_KEY` available for manual demo (optional for automated tests)

---

## Must-pass criteria

### GroundingValidator

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E4.1 | Valid ids pass | All shortlist ids accepted | ☐ |
| E4.2 | Unknown id fails | Id not in shortlist → validation failure | ☐ |
| E4.3 | Duplicate ids fail | Same id twice → validation failure | ☐ |
| E4.4 | Count ≤ topN | More than topN recommendations rejected or truncated per design | ☐ |

### FallbackRecommendationService

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E4.5 | Fallback uses pre-rank order | Top items match pre-rank when LLM mocked to fail | ☐ |
| E4.6 | Template explanations present | Each item has non-empty explanation string | ☐ |
| E4.7 | `meta.degraded = true` on fallback | Response flag set | ☐ |

### RecommendationOrchestrator

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E4.8 | Happy path end-to-end | Mocked LLM → 200 with recommendations | ☐ |
| E4.9 | No LLM call when filter empty | Zero candidates → suggestions only; verify mock not called | ☐ |
| E4.10 | LLM retry on grounding failure | Second call or fallback after invalid ids | ☐ |
| E4.11 | Enrich from repository | rating/cost match repository, not LLM invention | ☐ |
| E4.12 | `meta.candidatesConsidered` accurate | Matches shortlist size before LLM | ☐ |

### REST API

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E4.13 | `POST /api/v1/recommendations` exists | Returns 200 on valid body | ☐ |
| E4.14 | Response schema complete | Each item: restaurantId, name, cuisines, rating, costForTwo, explanation | ☐ |
| E4.15 | `summary` field present | Non-empty on success | ☐ |
| E4.16 | Bean Validation on request | Missing location → 400 | ☐ |
| E4.17 | Invalid budget enum → 400 | | ☐ |
| E4.18 | `min_rating` out of range → 400 | e.g. 6.0 or -1 | ☐ |
| E4.19 | `top_n` configurable | Request `topN: 3` returns ≤3 items | ☐ |
| E4.20 | Default `top_n` from config | Omit topN → uses default (e.g. 5) | ☐ |

### Problem statement success criteria (MVP)

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E4.21 | Grounded recommendations | Every `restaurantId` ∈ filtered shortlist | ☐ |
| E4.22 | Actionable empty filter | 200 + suggestions, no 500 | ☐ |
| E4.23 | Free-text passed to LLM | additionalPreferences in prompt (WireMock capture) | ☐ |

### Testing

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E4.24 | `@SpringBootTest` orchestrator test | Mocked LlmGateway, full flow | ☐ |
| E4.25 | MockMvc E2E test | POST recommendations with fixture profile | ☐ |
| E4.26 | Grounding unit tests | Invalid/duplicate id cases | ☐ |

---

## Should-pass criteria

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E4.27 | Manual live recommendation | Real API key → sensible ranked results | ☐ |
| E4.28 | Fewer matches than topN | Returns available count without error | ☐ |
| E4.29 | P95 latency under 15s | Informal timing with live LLM | ☐ |
| E4.30 | Impossible city via API | Empty + suggestions in JSON | ☐ |

---

## Edge cases to verify

From [edgecase.md](../edgecase.md): **F-06**, **F-07**, **F-08**, **L-09**–**L-14**, **O-01**–**O-06**, **A-01**–**A-06**, **A-12**

| ID | Scenario | Expected | Pass |
|----|----------|----------|------|
| F-06 | Zero filter matches | 200, suggestions, no LLM | ☐ |
| F-07 | One match, topN=5 | ≤1 recommendation | ☐ |
| L-11 | LLM returns fake id | Retry/fallback; no fake id in response | ☐ |
| L-12 | Duplicate ids from LLM | Rejected; fallback | ☐ |
| O-01 | LLM always fails | degraded=true, still 200 with fallback | ☐ |
| O-05 | candidates_considered=0 | LLM not invoked | ☐ |
| A-01 | Missing `location` | 400 | ☐ |
| A-12 | Script in free text | No crash; no prompt injection | ☐ |

---

## Sample request

```bash
curl -s -X POST http://localhost:8080/api/v1/recommendations \
  -H "Content-Type: application/json" \
  -d '{
    "location": "Bangalore",
    "budget": "MEDIUM",
    "cuisine": "Italian",
    "minRating": 4.0,
    "additionalPreferences": "quiet place for a date",
    "topN": 5
  }'
```

Expected: `recommendations` array (length ≤5), `summary`, `meta.candidatesConsidered` > 0 when data matches.

---

## Automated test gate

```bash
mvn -q clean test -Dtest="*Orchestrator*,*Recommendation*,*Grounding*,*Fallback*"
```

Must pass **without** live `OPENAI_API_KEY`.

---

## Sign-off

| Role | Name | Date | Approved |
|------|------|------|----------|
| Developer | | | ☐ |
| Reviewer (optional) | | | ☐ |

**Phase 4 complete when:** All **must-pass** (E4.1–E4.26) are checked.

**Next phase:** [phase-5-eval.md](./phase-5-eval.md)
