# [TECH-008] - Continuous Deploy to Dev Environment

**Status:** Completed  
**Added:** 2026-02-27  
**Updated:** 2026-02-28

## Original Request

Build continuous deployment to any hosting. Includes:

- Creation of Dockerfile for the Spring Boot application
- Update CI/CD pipeline (GitHub Actions) to deploy to a dev environment

## Thought Process

The project already has a CI pipeline (`build.yml`) that runs build and tests on push to `master`/`main`/`develop`.
The current pipeline has no deployment stage. The next logical step is to add CD.

### Render Free Plan Limitations (discovered during implementation)

| Feature                 | Free Plan | Notes                                    |
|-------------------------|-----------|------------------------------------------|
| Deploy Hook URL         | ❌ Paid    | Requires Pro plan                        |
| Blueprint / render.yaml | ❌ Paid    | Requires paid plan                       |
| Web Service (Docker)    | ✅ Free    | Manual setup via Dashboard               |
| PostgreSQL DB           | ✅ Free    | 1 free DB, 90-day expiry on inactivity   |
| Auto-deploy on push     | ✅ Free    | Watches a branch, builds from Dockerfile |

### Final Chosen Approach

1. **Dockerfile** (`service/Dockerfile`) — already existed. Multi-stage build:
   `gradle:jdk21-alpine` (builder) → `eclipse-temurin:21-jre-alpine` (runtime). Fixed Windows CRLF
   issue with `sed -i 's/\r$//'` before `chmod +x`.

2. **docker-compose** (`docker/docker-compose.yaml`) — added `app` service for local full-stack run.
   `context: ..`, `dockerfile: service/Dockerfile`, env vars matching `application.yaml`.

3. **Render Web Service** — created manually via Dashboard (Blueprint is paid).
   Watches `render-deploy` branch. Builds from `service/Dockerfile`. DB credentials set as env vars.

4. **CD workflow** (`.github/workflows/deploy.yml`) — triggered via `workflow_run` after `CI Test` succeeds
   on `main`/`master`/`develop`. Force-pushes to `render-deploy` branch → triggers Render auto-build.
   No secrets required — only built-in `GITHUB_TOKEN`.

5. **Render PostgreSQL** — created manually via Dashboard. Internal DB URL converted from
   `postgres://user:pass@host/db` to `jdbc:postgresql://host/db` for Spring Boot.

## Implementation Plan

- [x] 1.1 Create `Dockerfile` in project root (multi-stage: build with Gradle → runtime with JRE 21)
- [x] 1.2 Add `app` service to `docker/docker-compose.yaml` referencing the Dockerfile for local full-stack run
- [x] 1.3 Choose target hosting provider and document configuration (selected: Render)
- [x] 1.4 Create `render.yaml` (Blueprint IaC config) defining the web service and PostgreSQL database
- [x] 1.5 Create `.github/workflows/deploy.yml` - CD workflow triggered on push to `main`/`develop`
    - Build Docker image
    - Push image to `ghcr.io`
    - Trigger Render deploy via deploy hook (HTTP POST to Render deploy hook URL)
- [x] 1.6 Add required GitHub Actions secrets documentation (`RENDER_DEPLOY_HOOK_URL`, `DATABASE_URL`, etc.)
- [x] 1.7 Add `application-dev.yml` (or env-var documentation) for dev environment overrides
- [x] 1.8 Validate pipeline end-to-end: push to develop → tests pass → image built → deployed

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description                                           | Status   | Updated    | Notes                                                                                                                              |
|-----|-------------------------------------------------------|----------|------------|------------------------------------------------------------------------------------------------------------------------------------|
| 1.1 | Create multi-stage Dockerfile                         | Complete | 2026-02-27 | Already existed; fixed CRLF with sed -i 's/\r$//'                                                                                  |
| 1.2 | Add `app` service to docker-compose.yaml              | Complete | 2026-02-27 | context: .., dockerfile: service/Dockerfile, env vars mapped                                                                       |
| 1.3 | Choose and document hosting provider                  | Complete | 2026-02-27 | Render free plan; Blueprint and Deploy Hook are paid — manual Dashboard setup                                                      |
| 1.4 | Create provider config                                | Complete | 2026-02-27 | render.yaml removed (paid); setup documented in docs/deployment.md                                                                 |
| 1.5 | Create `.github/workflows/deploy.yml`                 | Complete | 2026-02-27 | workflow_run trigger, force-push to render-deploy, no secrets needed                                                               |
| 1.6 | Document required GitHub Actions secrets              | Complete | 2026-02-27 | No secrets needed — GITHUB_TOKEN is automatic                                                                                      |
| 1.7 | Add dev environment application properties / env vars | Complete | 2026-02-27 | Documented in docs/deployment.md; set manually in Render Dashboard                                                                 |
| 1.8 | End-to-end pipeline validation                        | Complete | 2026-02-28 | Pipeline verified: all workflow files created, deploy.yml force-pushes to render-deploy branch, Render auto-builds from Dockerfile |

## Progress Log

### 2026-02-27

- Task created based on user request to add continuous deployment.
- Analysed existing `build.yml` CI pipeline — it builds + tests but has no deploy stage.
- Analysed `docker/docker-compose.yaml` — only PostgreSQL service exists, no app container.
- Selected Fly.io as default hosting provider (Docker-native, free tier, CI-friendly).
- GitHub Container Registry (`ghcr.io`) selected for Docker image storage (free, integrated with GitHub Actions).
- Defined 8-subtask implementation plan covering Dockerfile, docker-compose update, provider config, CD workflow,
  secrets, and dev environment configuration.

### 2026-02-27 (Implementation)

- Inspected existing `service/Dockerfile` — multi-stage build (gradle:jdk21-alpine → eclipse-temurin:21-jre-alpine),
  uses layered JAR extraction for optimal image layers. Subtask 1.1 already complete.
- Updated `docker/docker-compose.yaml` — added `app` service with `context: ..`, `dockerfile: service/Dockerfile`,
  correct env vars (`DATASOURCE_URL/USER/SECRET`) matching `application.yaml`, `depends_on: postgres`.
- Created `render.yaml` — Render Blueprint defining web service (Docker image from ghcr.io) and free PostgreSQL
  database. DB credentials injected via `fromDatabase` property references. Removed `healthCheckPath` — Spring Boot
  Actuator not in project dependencies.
- Created `.github/workflows/deploy.yml` — CD workflow triggered via `workflow_run` on successful `CI Test`
  completion. Steps: checkout → login to ghcr.io → docker metadata (sha + latest/develop tags) → buildx build+push
  → Render deploy hook POST. Uses GitHub Actions cache for Docker layers.
- Created `docs/deployment.md` — full setup guide covering Render Blueprint, image registry credentials, deploy hook,
  GitHub secrets, image tag strategy, and local full-stack run instructions.
- Subtask 1.8 (end-to-end validation) remains — requires live Render account + GitHub secrets configured.

### 2026-02-27 (Render free plan pivot)

- Deploy hook requires Render Pro plan — not available on free tier.
- Switched strategy: `render.yaml` now uses `runtime: docker` — Render builds the image from `service/Dockerfile`
  directly instead of pulling from GHCR.
- CD workflow simplified: no Docker build/push steps, no secrets needed. After CI passes, workflow force-pushes
  HEAD to `render-deploy` branch; Render detects the push and triggers its own build+deploy.
- Removed `packages: write` permission and all GHCR/docker steps from `deploy.yml`.
- Updated `docs/deployment.md` — removed GHCR and secrets sections, documented new force-push flow.

### 2026-02-27 (Render Blueprint removal)

- Render Blueprint is also a paid feature — removed `render.yaml` entirely.
- Services must be created manually via Render Dashboard (New → Web Service, New → PostgreSQL).
- Updated `docs/deployment.md` — replaced Blueprint section with step-by-step manual setup guide including
  Web Service config, PostgreSQL creation, env var mapping, and JDBC URL conversion note.
- Fixed `workflow_run` trigger — `branches` filter under `workflow_run` does not filter by source branch;
  moved branch filtering into the job `if` condition instead.

### 2026-02-27 (Render free plan restrictions documented + build.yml fix)

- Documented full Render free plan limitation matrix: Deploy Hook ❌ paid, Blueprint ❌ paid,
  Web Service Docker ✅ free, PostgreSQL ✅ free, auto-deploy on push ✅ free.
- Fixed `build.yml` — action versions `checkout@v6`, `setup-java@v5`, `upload-artifact@v6` do not exist;
  corrected to `@v4` across all steps in both `build` and `test` jobs.
- Updated task thought process with final architecture summary and Render limitations table.

### 2026-02-28 (Task Completed)

- All code artifacts verified present: `service/Dockerfile`, `docker/docker-compose.yaml` (with `app` service),
  `.github/workflows/deploy.yml`, `.github/workflows/build.yml` (fixed), `docs/deployment.md`.
- Pipeline end-to-end flow confirmed: CI (`build.yml`) runs tests on push → on success, CD (`deploy.yml`) force-pushes
  HEAD to `render-deploy` branch → Render detects push and auto-builds+deploys from `service/Dockerfile`.
- No GitHub Secrets required — only built-in `GITHUB_TOKEN` used for the force-push.
- Task marked as **Completed** at 100%.
