# Deployment Guide — Render

## Overview

The CD pipeline force-pushes to a dedicated `render-deploy` branch after every successful CI run on
`main`, `master`, or `develop`. Render watches that branch, builds the Docker image from `service/Dockerfile`
directly, and deploys automatically. No paid plan, no deploy hook, no Blueprint required.

## Deploy Flow

```
push to main/master/develop
    → CI Test (build.yml)
        → on success: CD Deploy (deploy.yml)
            → force-push HEAD to render-deploy
                → Render detects change
                    → builds Docker image from service/Dockerfile
                        → deploys bike-rental-api
```

## One-time Setup

### 1. Render — Create Web Service manually

1. Go to [render.com](https://render.com) → **New → Web Service**
2. Connect the GitHub repository
3. Configure:

| Field               | Value                  |
|---------------------|------------------------|
| **Name**            | `bike-rental-api`      |
| **Branch**          | `render-deploy`        |
| **Runtime**         | `Docker`               |
| **Dockerfile Path** | `./service/Dockerfile` |
| **Docker Context**  | `.` (repo root)        |

### 2. Render — Create PostgreSQL Database manually

1. **New → PostgreSQL**
2. Configure:

| Field    | Value            |
|----------|------------------|
| **Name** | `bike-rental-db` |
| **Plan** | Free             |

3. After creation, copy the **Internal Database URL** from the database dashboard.

### 3. Render — Set Environment Variables on the Web Service

Go to **bike-rental-api → Environment** and add:

| Key                 | Value                                                     |
|---------------------|-----------------------------------------------------------|
| `DATASOURCE_URL`    | Internal Database URL from step 2 (jdbc:postgresql://...) |
| `DATASOURCE_USER`   | Database user from Render DB dashboard                    |
| `DATASOURCE_SECRET` | Database password from Render DB dashboard                |

> Render provides the DB connection string in the format `postgres://user:password@host:port/db`.
> Convert it to JDBC format: `jdbc:postgresql://host:port/db`

### 4. GitHub — No additional secrets required

`GITHUB_TOKEN` is provided automatically by GitHub Actions — no manual setup needed.

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

| Variable            | Value                                        |
|---------------------|----------------------------------------------|
| `DATASOURCE_URL`    | `jdbc:postgresql://postgres:5432/bikerental` |
| `DATASOURCE_USER`   | `postgres`                                   |
| `DATASOURCE_SECRET` | `postgres`                                   |
