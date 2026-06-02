# Phase 6 Evaluation: UI (Optional)

**Phase goal:** Thymeleaf web UI for submitting preferences and viewing recommendations without Postman.  
**Plan reference:** [implementationPlan.md § Phase 6](../implementationPlan.md#phase-6-ui-optional)  
**Architecture reference:** [architecture.md §1.2, §6.1](../architecture.md)

---

## Entry criteria

- [ ] [Phase 4 eval](./phase-4-eval.md) signed off
- [ ] [Phase 5 eval](./phase-5-eval.md) recommended (error handling for UI flows)

---

## Must-pass criteria

### Pages & navigation

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E6.1 | Home/form page at `/` | Loads in browser without 500 | ☐ |
| E6.2 | Form fields present | location, budget, cuisine, min rating, additional preferences | ☐ |
| E6.3 | Submit reaches API | POST to orchestrator via controller or REST client | ☐ |
| E6.4 | Results page displays recommendations | name, cuisines, rating, cost, explanation per card | ☐ |
| E6.5 | Summary displayed | Overall `summary` text visible | ☐ |

### Metadata integration

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E6.6 | City dropdown populated | Values from metadata service / API | ☐ |
| E6.7 | Budget dropdown | LOW / MEDIUM / HIGH options | ☐ |

### Empty & degraded states

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E6.8 | Zero results UI | Suggestions from API shown (not blank page) | ☐ |
| E6.9 | Degraded banner | When `meta.degraded=true`, user-visible notice | ☐ |
| E6.10 | API error page | 503/500 shows friendly message + retry hint | ☐ |

### Validation & security

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E6.11 | Client-side required fields | Empty submit blocked or server 400 shown | ☐ |
| E6.12 | XSS safe output | Free text with `<script>` escaped in Thymeleaf | ☐ |

---

## Should-pass criteria

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E6.13 | Readable layout | Basic CSS; mobile-tolerable width | ☐ |
| E6.14 | Double-submit prevention | Button disabled after first click | ☐ |
| E6.15 | Cuisine hint or autocomplete | Optional text field with examples | ☐ |
| E6.16 | `topN` selector on form | Optional advanced field | ☐ |

---

## Edge cases to verify

From [edgecase.md](../edgecase.md): **U-01**–**U-07**

| ID | Scenario | Expected | Pass |
|----|----------|----------|------|
| U-01 | Empty required fields | Validation message | ☐ |
| U-02 | API 503 on submit | Error page, not stack trace | ☐ |
| U-03 | Zero results | Suggestions visible | ☐ |
| U-04 | degraded=true | Banner shown | ☐ |
| U-05 | XSS in free text | Escaped on results page | ☐ |
| U-06 | Double click submit | No duplicate duplicate requests | ☐ |

---

## Manual walkthrough script

1. Start app: `mvn spring-boot:run`
2. Open `http://localhost:8080/`
3. Select valid city, MEDIUM budget, known cuisine, rating 4.0
4. Submit → verify ≥1 recommendation card
5. Submit impossible combo (unknown city) → verify suggestions
6. (Optional) Stop LLM / invalid key → verify degraded or error UI
7. Enter `<b>test</b>` in free text → verify escaped display

---

## Automated test gate

Phase 6 is primarily manual. Optional:

- Selenium/Playwright smoke (if added later)
- Thymeleaf view tests with `MockMvc` for form GET 200

```bash
mvn -q test -Dtest="*Ui*,*Thymeleaf*"
```

---

## Sign-off

| Role | Name | Date | Approved |
|------|------|------|----------|
| Developer | | | ☐ |

**Phase 6 complete when:** All **must-pass** (E6.1–E6.12) are checked.

**MVP note:** Phases 0–5 constitute the required MVP; Phase 6 is optional polish.
