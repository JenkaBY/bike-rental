# Deployment Guide — Render

## Overview

The CD pipeline builds a Docker image, pushes it to GitHub Container Registry (`ghcr.io`), and triggers a deploy
on [Render](https://render.com) via a deploy hook after every successful CI run on `main`, `master`, or `develop`.

## One-time Setup

### 1. Render — Create Services via Blueprint

1. Go to [render.com](https://render.com) → **New → Blueprint**
2. Connect the GitHub repository
3. Render will detect `render.yaml` and create:
   - **Web Service** `bike-rental-api` (Docker image from `ghcr.io`)
   - **PostgreSQL Database** `bike-rental-db` (free tier)

### 2. Render — Configure Image Registry Credentials

The web service pulls from `ghcr.io` (GitHub Container Registry, public image).

If the image is **private**:

1. In Render dashboard → **bike-rental-api** → **Settings → Image**
2. Add credentials:
   - **Registry**: `ghcr.io`
   - **Username**: your GitHub username
   - **Password**: a GitHub PAT with `read:packages` scope

### 3. Render — Get Deploy Hook URL

1. Render dashboard → **bike-rental-api** → **Settings → Deploy Hook**
2. Copy the URL (looks like `https://api.render.com/deploy/srv-xxx?key=yyy`)

### 4. GitHub — Add Repository Secrets

Go to **GitHub repository → Settings → Secrets and variables → Actions → New repository secret**:

| Secret name              | Value                                     |
|--------------------------|-------------------------------------------|
| `RENDER_DEPLOY_HOOK_URL` | Deploy hook URL copied from Render        |

> `GITHUB_TOKEN` is provided automatically by GitHub Actions — no manual setup needed for pushing to `ghcr.io`.

### 5. Render — Set `GHCR_IMAGE_NAME` environment variable

In Render dashboard → **bike-rental-api** → **Environment**:

| Key               | Value                                       |
|-------------------|---------------------------------------------|
| `GHCR_IMAGE_NAME` | `<github-owner>/bike-rental:latest`         |

Example: `jenkaby/bike-rental:latest`

---

## Image Tags

| Branch      | Tag            |
|-------------|----------------|
| `main`      | `latest`, `<sha>` |
| `master`    | `latest`, `<sha>` |
| `develop`   | `develop`, `<sha>` |

---

## Local Full-Stack Run

To run the full application stack locally (app + database):

```bash
# From the project root
cd docker
docker compose up --build
```

The application will be available at `http://localhost:8080`.

Environment variables used by the `app` service (configured in `docker-compose.yaml`):

| Variable          | Value                                        |
|-------------------|----------------------------------------------|
| `DATASOURCE_URL`  | `jdbc:postgresql://postgres:5432/bikerental` |
| `DATASOURCE_USER` | `postgres`                                   |
| `DATASOURCE_SECRET` | `postgres`                                 |

