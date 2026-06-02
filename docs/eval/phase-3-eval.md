# Phase 3 Evaluation: LLM Integration

**Phase goal:** Prompt building, WebClient LLM gateway, JSON parsing—callable independent of full orchestrator.  
**Plan reference:** [implementationPlan.md § Phase 3](../implementationPlan.md#phase-3-llm-integration)  
**Architecture reference:** [architecture.md §3.4, §3.5](../architecture.md)

---

## Entry criteria

- [ ] [Phase 2 eval](./phase-2-eval.md) signed off
- [ ] `FilterService` can produce a shortlist for test preferences
- [ ] `OPENAI_API_KEY` available for **manual** checks only (not CI)

---

## Must-pass criteria

### PromptBuilder

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E3.1 | Template loads from classpath | `prompts/recommend-v1.txt` present | ☐ |
| E3.2 | Preferences embedded | Location, budget, cuisine, rating, free text in prompt | ☐ |
| E3.3 | Candidates JSON embedded | Only shortlist restaurants; capped count | ☐ |
| E3.4 | Grounding instruction present | Text forbids inventing restaurants | ☐ |
| E3.5 | `topN` reflected in task section | Matches request parameter | ☐ |

### LlmGateway

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E3.6 | `LlmGateway` interface + OpenAI adapter | Bean wired via `LlmGatewayConfig` | ☐ |
| E3.7 | WebClient timeout configured | Hung call fails within `app.llm.timeout` | ☐ |
| E3.8 | Missing API key handled | Clear error (startup or call time per design) | ☐ |
| E3.9 | WireMock integration test passes | No live API key in CI | ☐ |

### LlmResponseParser

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E3.10 | Valid JSON parsed | Maps to `recommendations` + `summary` | ☐ |
| E3.11 | Markdown-fenced JSON parsed | Strip ```json fences | ☐ |
| E3.12 | Malformed JSON throws `LlmException` | Unit test with garbage input | ☐ |
| E3.13 | Empty recommendations array handled | Documented behavior (exception or empty) | ☐ |

### Configuration

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E3.14 | Provider selectable via config | `app.llm.provider` documented in README | ☐ |
| E3.15 | Model and temperature from config | Visible in outbound request (WireMock capture) | ☐ |

---

## Should-pass criteria

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E3.16 | Manual live LLM call | Real key → sensible JSON for 5 candidates | ☐ |
| E3.17 | Retry on 429/503 (if implemented) | WireMock simulates 503 then 200 | ☐ |
| E3.18 | Long free-text truncated | Prompt length bounded | ☐ |

---

## Edge cases to verify

From [edgecase.md](../edgecase.md): **L-01**–**L-08**, **L-15**–**L-16**, **L-19**, **T-02**

| ID | Scenario | Expected | Pass |
|----|----------|----------|------|
| L-01 | No API key | Fail gracefully | ☐ |
| L-03 | Timeout | `LlmException` within configured duration | ☐ |
| L-06 | Non-JSON response | Parser error | ☐ |
| L-07 | Fenced JSON | Parse success | ☐ |
| L-15 | 20 large candidates | Prompt within limits or explicit truncation | ☐ |
| T-02 | CI without key | WireMock test only | ☐ |

---

## Automated test gate

```bash
mvn -q clean test -Dtest="*Llm*,*Prompt*,*WireMock*"
```

Must pass **without** `OPENAI_API_KEY` set.

---

## Manual live check (optional, not CI)

```text
1. Set OPENAI_API_KEY
2. Run PromptBuilder + LlmGateway with 3–5 fixture restaurants
3. Confirm JSON structure and ids match input list
```

---

## Sign-off

| Role | Name | Date | Approved |
|------|------|------|----------|
| Developer | | | ☐ |

**Phase 3 complete when:** All **must-pass** (E3.1–E3.15) are checked.

**Next phase:** [phase-4-eval.md](./phase-4-eval.md)
