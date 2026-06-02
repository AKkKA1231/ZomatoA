# Phase 5 Evaluation: Quality & Operations

**Phase goal:** Production-minded errors, health, observability, Docker, CI-safe tests, and documentation. **Final MVP gate before optional UI.**  
**Plan reference:** [implementationPlan.md § Phase 5](../implementationPlan.md#phase-5-quality--operations)  
**Architecture reference:** [architecture.md §5.3, §6, §7](../architecture.md)

---

## Entry criteria

- [ ] [Phase 4 eval](./phase-4-eval.md) signed off
- [ ] Core recommend API functional

---

## Must-pass criteria

### Exception handling

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E5.1 | `GlobalExceptionHandler` registered | Consistent JSON error body | ☐ |
| E5.2 | Validation errors → 400 | Field-level messages for invalid DTO | ☐ |
| E5.3 | Malformed JSON → 400 | Invalid body syntax | ☐ |
| E5.4 | `LlmException` → 503 | Code `LLM_UNAVAILABLE` (or documented equivalent) | ☐ |
| E5.5 | Unhandled errors → 500 | Includes `traceId` (or correlation id) | ☐ |
| E5.6 | Empty body `{}` → 400 | Missing required fields | ☐ |

### Health & data readiness

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E5.7 | `RestaurantDataHealthIndicator` | DOWN when cache missing; UP after load | ☐ |
| E5.8 | `/actuator/health` reflects data | Custom indicator visible in response | ☐ |
| E5.9 | App starts with valid cache | Health UP after fixture/full load | ☐ |

### Security & admin

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E5.10 | Admin ingest not public in prod | `@Profile("dev")` or API-key required | ☐ |
| E5.11 | No secrets in logs | Grep logs after LLM call — no API key substring | ☐ |
| E5.12 | `.env` not committed | Only `.env.example` in repo | ☐ |

### Observability

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E5.13 | MDC `traceId` on requests | Present in logs for API call | ☐ |
| E5.14 | Request logging | Filter count / LLM latency logged at INFO/DEBUG | ☐ |

### Docker & CI

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E5.15 | `Dockerfile` builds | `docker build` succeeds | ☐ |
| E5.16 | Container runs with cache | Health UP with mounted or baked `data/processed/` | ☐ |
| E5.17 | `mvn test` in CI mode | No HF download; no API key; all tests green | ☐ |
| E5.18 | Fixture-only data in tests | `restaurants-small.json` drives integration tests | ☐ |

### Documentation

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E5.19 | README complete | Run, ingest, env vars, curl example | ☐ |
| E5.20 | Links to docs | problemStatement, architecture, implementationPlan, edgecase, eval | ☐ |

### Testing coverage

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E5.21 | `@WebMvcTest` for controllers | Validation + error status codes | ☐ |
| E5.22 | Exception handler tests | 400/503/500 mappings | ☐ |

---

## Should-pass criteria

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E5.23 | springdoc-openapi | Swagger UI lists `/api/v1` endpoints | ☐ |
| E5.24 | Micrometer metrics | `recommendation.latency` or equivalent registered | ☐ |
| E5.25 | Docker without API key | Recommend returns degraded/fallback or 503 per design | ☐ |
| E5.26 | ProblemDetail (RFC 7807) | Optional structured error format | ☐ |

---

## Edge cases to verify

From [edgecase.md](../edgecase.md): **D-02**–**D-04**, **M-01**, **C-05**, **S-01**–**S-02**, **O-01**–**O-05** (ops), **A-07**–**A-11**, **T-01**–**T-03**

| ID | Scenario | Expected | Pass |
|----|----------|----------|------|
| D-02 | Missing cache on startup | Health DOWN | ☐ |
| D-03 | Corrupt cache file | Health DOWN; clear error | ☐ |
| C-05 | Admin ingest without dev profile | 401/403 or 404 | ☐ |
| S-01 | API key never logged | Manual log review | ☐ |
| A-07 | Malformed JSON | 400 | ☐ |
| A-11 | Huge request body | 413 or 400 | ☐ |
| T-01 | CI without network | Tests pass offline | ☐ |

---

## MVP milestone checklist

After Phase 5, confirm all [problem statement success criteria](../problemStatement.md#success-criteria-definition-of-done):

| Criterion | Verified by |
|-----------|-------------|
| Dataset loads reliably | E5.7–E5.9 |
| Top-N recommendations | Phase 4 + E5.17 |
| Full response fields | Phase 4 |
| Grounded results | Phase 4 E4.21 |
| Actionable failures | E5.2, E5.4, Phase 4 E4.22 |

---

## Automated test gate

```bash
mvn -q clean verify
docker build -t zomato-recommendation:local .
```

---

## Sign-off

| Role | Name | Date | Approved |
|------|------|------|----------|
| Developer | | | ☐ |
| Reviewer (optional) | | | ☐ |

**Phase 5 complete when:** All **must-pass** (E5.1–E5.22) and MVP milestone checklist are checked.

**Next phase (optional):** [phase-6-eval.md](./phase-6-eval.md)
