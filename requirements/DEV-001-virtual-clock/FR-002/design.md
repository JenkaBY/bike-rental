# System Design: FR-002 - Default System Clock Bean (shared)

## 1. Architectural Overview

This story establishes the production-safe default `Clock` bean. No new source file is created. An existing shared
infrastructure configuration class in the `shared` module gains a single `@Bean Clock clock()` method that returns
`Clock.systemUTC()`. This bean is the fallback for all profiles that do not activate the dev-time controller — most
notably `prod`.

When the `dev` or `test` profile is active, `DevTimeController` (FR-004) registers a `@Primary Clock` bean that
overrides this default. When neither profile is active, the system-clock bean is the only `Clock` in the context.

## 2. Impacted Components

* **Existing shared infrastructure configuration class (`shared` module):** Gains one `@Bean Clock clock()` method
  returning `Clock.systemUTC()`. No new class or file is created; this is a one-method addition to an already-existing
  file.

## 3. Abstract Data Schema Changes

No database entities are introduced or changed. `Clock.systemUTC()` holds no state.

## 4. Component Contracts & Payloads

* **Interaction: Business Service → system `Clock` bean**
    * **Protocol:** In-process Java method call
    * **Payload:** `clock.instant()` returns the real current UTC `Instant`. All other standard `Clock` methods delegate
      to the JVM system clock.

## 5. Updated Interaction Sequence

1. Application starts with a production (non-dev/test) profile.
2. The `DevTimeController` class is not instantiated — its `@Profile({"dev","test"})` condition is not satisfied.
3. The shared configuration class registers `Clock.systemUTC()` as the sole `Clock` bean.
4. Business services that inject `Clock` receive the system-time implementation.
5. All `clock.instant()` calls return real system time.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** `Clock.systemUTC()` has no mutable state and presents no security surface.
* **Scale & Performance:** Zero overhead beyond a native clock lookup; thread-safe by definition.
