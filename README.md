# ZOMATO — AI-Powered Restaurant Recommendation

Spring Boot service that recommends restaurants using structured filters and an LLM, grounded in the [Zomato Hugging Face dataset](https://huggingface.co/datasets/ManikaSaini/zomato-restaurant-recommendation).

## Prerequisites

- **Java 17+** (JDK 21 recommended; enforcer allows 17–22)
- **Maven 3.9+** or use the included Maven Wrapper (`mvnw`)

## Quick start

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

With the **dev** profile:

```bash
mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Health check:

```bash
curl http://localhost:8080/actuator/health
```

## Environment variables

| Variable | Description |
|----------|-------------|
| `OPENAI_API_KEY` | API key for the LLM provider (needed from Phase 3 onward) |

Copy `.env.example` to `.env` locally. Do not commit `.env`.

## Build & test

```bash
mvnw clean test
mvnw clean package
```

## Configuration

Main settings live in `src/main/resources/application.yml` under the `app.*` prefix. See [docs/architecture.md](docs/architecture.md) for details.

## Documentation

| Doc | Description |
|-----|-------------|
| [problemStatement.md](docs/problemStatement.md) | Goals and success criteria |
| [architecture.md](docs/architecture.md) | System design |
| [implementationPlan.md](docs/implementationPlan.md) | Phased build plan |
| [edgecase.md](docs/edgecase.md) | Edge cases |
| [eval/](docs/eval/) | Per-phase evaluation checklists |
| [deploymentPlan.md](docs/deploymentPlan.md) | Deployment instructions (Vercel & Backend) |

## Frontend Integration

The backend API is pre-configured with **CORS support** allowing requests from `http://localhost:*` and `https://*.vercel.app`. This makes it ready to connect with a Vercel-hosted frontend out of the box. See the deployment plan for full details.

## Data ingest (dev profile)

Place a Zomato CSV under `data/raw/`, then:

```bash
mvnw spring-boot:run -Dspring-boot.run.profiles=dev
curl -X POST http://localhost:8080/api/v1/admin/ingest
```

Processed output: `data/processed/restaurants.json` and `metadata.json`.

## Project status

**Phase 1** — Data pipeline (ingest, cache, in-memory repository, dev ingest API).

**Phase 0** — Project foundation (Spring Boot skeleton, configuration, Actuator health).
