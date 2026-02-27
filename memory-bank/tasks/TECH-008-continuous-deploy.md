# [TECH-008] - Continuous Deploy to Dev Environment

**Status:** Pending  
**Added:** 2026-02-27  
**Updated:** 2026-02-27

## Original Request

Build continuous deployment to any hosting. Includes:

- Creation of Dockerfile for the Spring Boot application
- Update CI/CD pipeline (GitHub Actions) to deploy to a dev environment

## Thought Process

The project already has a CI pipeline (`build.yml`) that runs build and tests on push to `master`/`main`/`develop`.
The current pipeline has no deployment stage. The next logical step is to add CD:

1. **Dockerfile** - Package the Spring Boot fat JAR into a Docker image. The `service` module is the deployable unit.
   Java 21 (Amazon Corretto) should be used to match the CI build environment. A multi-stage build is preferred to
   keep the image lean.

2. **Docker Compose update** - Extend `docker/docker-compose.yaml` to include the application service alongside
   PostgreSQL, for local and dev environment usage.

3. **GitHub Actions CD workflow** (or extend `build.yml`) - After successful build + tests on `develop`/`main`:
    - Build and push the Docker image to a container registry (GitHub Container Registry `ghcr.io` is the natural
      choice as it's free and tightly integrated with GitHub Actions).
    - Deploy to a dev hosting environment. Suitable free/cheap options:
        - **Railway** - simple Dockerfile-based deploy, free tier, supports PostgreSQL add-on.
        - **Render** - similar to Railway, supports Docker and PostgreSQL.
        - **Fly.io** - Docker-native PaaS, generous free tier.
    - The task will target **Render** as the chosen provider (Docker-native, free tier, PostgreSQL add-on,
      deploy-hook or GitHub-native integration for CI-triggered deployments).

4. **Secrets management** - Deployment credentials (Fly.io API token, DB URL, etc.) stored as GitHub Actions secrets.

5. **Environment configuration** - The application needs an `application-dev.yml` or env-var overrides for
   `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` and any other
   environment-specific properties.

## Implementation Plan

- [ ] 1.1 Create `Dockerfile` in project root (multi-stage: build with Gradle → runtime with JRE 21)
- [ ] 1.2 Add `app` service to `docker/docker-compose.yaml` referencing the Dockerfile for local full-stack run
- [ ] 1.3 Choose target hosting provider and document configuration (selected: Render)
- [ ] 1.4 Create `render.yaml` (Blueprint IaC config) defining the web service and PostgreSQL database
- [ ] 1.5 Create `.github/workflows/deploy.yml` - CD workflow triggered on push to `main`/`develop`
    - Build Docker image
    - Push image to `ghcr.io`
    - Trigger Render deploy via deploy hook (HTTP POST to Render deploy hook URL)
- [ ] 1.6 Add required GitHub Actions secrets documentation (`RENDER_DEPLOY_HOOK_URL`, `DATABASE_URL`, etc.)
- [ ] 1.7 Add `application-dev.yml` (or env-var documentation) for dev environment overrides
- [ ] 1.8 Validate pipeline end-to-end: push to develop → tests pass → image built → deployed

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                                           | Status      | Updated    | Notes                                                                     |
|-----|-------------------------------------------------------|-------------|------------|---------------------------------------------------------------------------|
| 1.1 | Create multi-stage Dockerfile                         | Not Started | 2026-02-27 | Java 21 Corretto, fat JAR from :service:build                             |
| 1.2 | Add `app` service to docker-compose.yaml              | Not Started | 2026-02-27 | Depends on 1.1                                                            |
| 1.3 | Choose and document hosting provider                  | Not Started | 2026-02-27 | Selected: Render                                                          |
| 1.4 | Create `render.yaml` Blueprint config                 | Not Started | 2026-02-27 | Defines web service + PostgreSQL add-on                                   |
| 1.5 | Create `.github/workflows/deploy.yml`                 | Not Started | 2026-02-27 | Triggered on main/develop push after tests; deploy via Render deploy hook |
| 1.6 | Document required GitHub Actions secrets              | Not Started | 2026-02-27 | RENDER_DEPLOY_HOOK_URL, DB credentials, etc.                              |
| 1.7 | Add dev environment application properties / env vars | Not Started | 2026-02-27 | Spring profile `dev` or environment variables                             |
| 1.8 | End-to-end pipeline validation                        | Not Started | 2026-02-27 | Push to develop, verify full deploy cycle                                 |

## Progress Log

### 2026-02-27

- Task created based on user request to add continuous deployment.
- Analysed existing `build.yml` CI pipeline — it builds + tests but has no deploy stage.
- Analysed `docker/docker-compose.yaml` — only PostgreSQL service exists, no app container.
- Selected Fly.io as default hosting provider (Docker-native, free tier, CI-friendly).
- GitHub Container Registry (`ghcr.io`) selected for Docker image storage (free, integrated with GitHub Actions).
- Defined 8-subtask implementation plan covering Dockerfile, docker-compose update, provider config, CD workflow,
  secrets, and dev environment configuration.




