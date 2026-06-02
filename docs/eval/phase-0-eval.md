# Phase 0 Evaluation: Project Foundation

**Phase goal:** Runnable Spring Boot skeleton with configuration, packages, and developer setup.  
**Plan reference:** [implementationPlan.md § Phase 0](../implementationPlan.md#phase-0-project-foundation)  
**Architecture reference:** [architecture.md §1.2, §8](../architecture.md)

---

## Entry criteria

- [ ] Java 21 JDK installed
- [ ] Maven or Maven Wrapper available
- [ ] Repository cloned locally

---

## Must-pass criteria

### Build & startup

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E0.1 | Project compiles with Java 21 | `mvn -q clean compile` exits 0 | ☐ |
| E0.2 | Application starts | `mvn spring-boot:run` — no startup exceptions | ☐ |
| E0.3 | Default port listening | `GET http://localhost:8080/actuator/health` returns 200 | ☐ |
| E0.4 | Context load test | `mvn test` — `@SpringBootTest` passes | ☐ |

### Structure & configuration

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E0.5 | Package layout exists | `api`, `config`, `domain`, `service`, `infrastructure` under `com.zomato.recommendation` | ☐ |
| E0.6 | `AppProperties` binds `app.*` | Startup with valid `application.yml`; no bind errors | ☐ |
| E0.7 | `application-dev.yml` profile loads | Run with `-Dspring.profiles.active=dev` | ☐ |
| E0.8 | Dependencies present | `pom.xml` includes web, validation, webflux, actuator, test | ☐ |

### Security & hygiene

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E0.9 | No secrets in Git | `.gitignore` includes `target/`, `data/processed/`, `.env`; no API keys in YAML | ☐ |
| E0.10 | `.env.example` documented | Lists `OPENAI_API_KEY` placeholder | ☐ |
| E0.11 | README prerequisites | Java version, run command, env vars mentioned | ☐ |

---

## Should-pass criteria (recommended)

| # | Criterion | How to verify | Pass |
|---|-----------|---------------|------|
| E0.12 | Maven Wrapper committed | `./mvnw` works on clean machine | ☐ |
| E0.13 | Invalid config fails fast | Set `app.recommendation.top-n: -1` — startup fails or validated | ☐ |

---

## Edge cases to spot-check

From [edgecase.md](../edgecase.md): **C-01**, **C-02**, **C-03**, **C-04**

| ID | Check | Pass |
|----|-------|------|
| C-01 | Malformed YAML prevents startup with readable error | ☐ |
| C-04 | `java.version` / enforcer requires 21 | ☐ |

---

## Automated test gate

```bash
mvn -q clean test
```

Expected: all tests green (minimum: context load test).

---

## Manual smoke script

```bash
mvn spring-boot:run
# separate terminal:
curl -s http://localhost:8080/actuator/health
```

Expected: JSON with `"status":"UP"` (or equivalent).

---

## Sign-off

| Role | Name | Date | Approved |
|------|------|------|----------|
| Developer | | | ☐ |
| Reviewer (optional) | | | ☐ |

**Phase 0 complete when:** All **must-pass** (E0.1–E0.11) are checked.

**Next phase:** [phase-1-eval.md](./phase-1-eval.md)
