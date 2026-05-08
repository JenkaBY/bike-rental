# System Design: FR-005 - Configuration & Application Properties

## 1. Architectural Overview

The feature introduces one new configuration property under the namespace `app.dev.virtual-clock`. Because
`DevTimeController` is the only new production file, the SSE interval value is injected directly into the controller's
constructor from the property source — no separate `@ConfigurationProperties` record class is created. This keeps the
single-file constraint intact while still honouring the property-key convention and supporting environment variable
overrides via Spring's relaxed binding.

## 2. Impacted Components

* **`DevTimeController` (the one new file):** Receives the SSE interval as a `long sseIntervalMs` constructor parameter,
  bound from `${app.dev.virtual-clock.sse-interval-ms:1000}`. No other component reads this property.

* **`application.yml` (existing file):** Gains an entry under the `app.dev.virtual-clock` namespace documenting the
  property and its default. No other existing file is modified.

## 3. Abstract Data Schema Changes

No database entities are introduced or changed.

## 4. Component Contracts & Payloads

* **Interaction: `application.yml` / environment → `DevTimeController` constructor**
    * **Protocol:** Spring property binding at startup
    * **Property details:**

      | Key | Type | Default | Env-var override |
          |---|---|---|---|
      | `app.dev.virtual-clock.sse-interval-ms` | positive integer (ms) | `1000` | `APP_DEV_VIRTUAL_CLOCK_SSE_INTERVAL_MS` |

      Example `application.yml` entry:
      ```yaml
      app:
        dev:
          virtual-clock:
            sse-interval-ms: 1000
      ```

## 5. Updated Interaction Sequence

1. Application starts; Spring Boot resolves `app.dev.virtual-clock.sse-interval-ms` from `application.yml` (or the
   environment variable override).
2. The resolved value is injected into `DevTimeController`'s constructor.
3. When a client opens the SSE stream, the controller uses the injected interval to schedule periodic emissions.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** No secrets in this namespace.
* **Scale & Performance:** The value is a primitive long captured at construction time — zero runtime overhead. Must
  validate to be greater than zero to prevent an immediate-completion loop in the SSE scheduler.
