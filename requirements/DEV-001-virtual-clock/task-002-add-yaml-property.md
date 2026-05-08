# Task 002: Add SSE interval property to application.yaml

> **Applied Skill:** `springboot.instructions.md` — typed configuration properties pattern, `@Value` injection for
> single dev-only value.

## 1. Objective

Add the `app.dev.virtual-clock.sse-interval-ms` property to `application.yaml` so the SSE emission interval for
`DevTimeController` is documented and can be overridden via the environment variable
`APP_DEV_VIRTUAL_CLOCK_SSE_INTERVAL_MS`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/resources/application.yaml`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

N/A — this is a YAML file.

**Code to Add/Replace:**

* **Location:** At the end of the `app:` block, after the `cors:` section (current last entry).

```yaml
  dev:
    virtual-clock:
      sse-interval-ms: 1000
```

The full `app:` section after the change:

```yaml
app:
  default-locale: en
  customers:
    search-limit-result: 10
  rental:
    time-increment: 5m
    forgiveness:
      overtime-duration: 7m
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS}
    allowed-methods: GET,POST,PUT,PATCH,DELETE,OPTIONS
    allowed-headers: "*"
    allow-credentials: false
    max-age: 3600
  dev:
    virtual-clock:
      sse-interval-ms: 1000
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
