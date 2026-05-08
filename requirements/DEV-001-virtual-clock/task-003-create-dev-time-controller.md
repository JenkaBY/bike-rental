# Task 003: Create DevClockConfig and TimeTravelController

> **Applied Skill:** `java.instructions.md` — Java 21, no-comments convention, expressive naming.
> **Applied Skill:** `springboot.instructions.md` — `@Profile`, `@Configuration` + `@Primary @Bean`, `@Value` property
> injection via constructor.

## 1. Objective

Create two new production source files for the DEV-001 feature:

1. `DevClockConfig` — a `@Configuration` class in the `shared/config` package that declares `SettableClock` (a `Clock`
   subclass with atomic mutable state) and registers it as the `@Primary Clock` bean under the `dev`/`test` profiles.
2. `TimeTravelController` — a pure `@RestController` that injects `SettableClock` and exposes three HTTP endpoints:
   `PUT /api/dev/time`, `GET /api/dev/time` (SSE), `DELETE /api/dev/time`.

Request/response DTOs and SSE events use `java.time.Instant` directly; Jackson handles ISO-8601 serialisation
automatically via Spring Boot's `JavaTimeModule` autoconfiguration.

## 2. File to Modify / Create

### File A

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/shared/config/DevClockConfig.java`
* **Action:** Create New File

### File B

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/shared/web/TimeTravelController.java`
* **Action:** Create New File

## 3. Code Implementation

### File A — DevClockConfig

**Imports Required:** all listed inside the snippet.

**Location:** New file — paste as-is.

```java
package com.github.jenkaby.bikerental.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Profile({"dev", "test"})
@Configuration
public class DevClockConfig {

    @Primary
    @Bean
    public SettableClock clock() {
        return new SettableClock();
    }

    public static final class SettableClock extends Clock {

        private final AtomicReference<Clock> delegate = new AtomicReference<>(Clock.systemUTC());
        private final AtomicBoolean fixed = new AtomicBoolean(false);

        public void setFixed(Instant instant) {
            delegate.set(Clock.fixed(instant, ZoneOffset.UTC));
            fixed.set(true);
        }

        public void reset() {
            delegate.set(Clock.systemUTC());
            fixed.set(false);
        }

        public boolean isFixed() {
            return fixed.get();
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return delegate.get().withZone(zone);
        }

        @Override
        public Instant instant() {
            return delegate.get().instant();
        }
    }
}
```

### File B — TimeTravelController

**Imports Required:** all listed inside the snippet.

**Location:** New file — paste as-is.

```java
package com.github.jenkaby.bikerental.shared.web;

import com.github.jenkaby.bikerental.shared.config.DevClockConfig.SettableClock;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Profile({"dev", "test"})
@RestController
@RequestMapping("/api/dev/time")
public class TimeTravelController {

    private final SettableClock settableClock;
    private final long sseIntervalMs;

    public DevTimeController(
            SettableClock settableClock,
            @Value("${app.dev.virtual-clock.sse-interval-ms:1000}") long sseIntervalMs
    ) {
        this.settableClock = settableClock;
        this.sseIntervalMs = sseIntervalMs;
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TimeResponse> setTime(@Valid @RequestBody SetTimeRequest request) {
        settableClock.setFixed(request.instant());
        return ResponseEntity.ok(new TimeResponse(settableClock.instant()));
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTime() {
        SseEmitter emitter = new SseEmitter(0L);
        var executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event()
                        .data(new TimeEvent(settableClock.instant(), settableClock.isFixed())));
            } catch (IOException e) {
                emitter.completeWithError(e);
                executor.shutdown();
            }
        }, 0, sseIntervalMs, TimeUnit.MILLISECONDS);
        emitter.onCompletion(executor::shutdown);
        emitter.onTimeout(executor::shutdown);
        return emitter;
    }

    @DeleteMapping
    public ResponseEntity<Void> resetTime() {
        settableClock.reset();
        return ResponseEntity.noContent().build();
    }

    public record SetTimeRequest(
            @NotNull(message = "instant must not be null")
            Instant instant
    ) {}

    public record TimeResponse(@NotNull Instant instant) {}

    public record TimeEvent(@NotNull Instant instant, boolean fixed) {}
}
```

### Key implementation notes for the Junior Agent

1. `DevClockConfig` carries `@Configuration` and is the only class with `@Primary @Bean`. `TimeTravelController` is a
   pure `@RestController` with no `@Configuration` — the responsibilities are cleanly separated.
2. The `@Bean` method in `DevClockConfig` returns `SettableClock` (not `Clock`) so Spring registers the bean under both
   types. `TimeTravelController` can inject `SettableClock` directly to call `setFixed`/`reset`/`isFixed` without
   casting.
3. `@Primary` on `clock()` ensures `SettableClock` overrides `ClockConfig.applicationClock()` when `dev` or `test`
   profile is active.
4. DTOs use `java.time.Instant` directly. Spring Boot's `JavaTimeModule` autoconfiguration serialises `Instant` as an
   ISO-8601 string (e.g., `"2026-01-01T00:00:00Z"`) because `spring.jackson.serialization.write-dates-as-timestamps`
   defaults to `false` in Spring Boot.
5. `@NotNull Instant instant` in `SetTimeRequest`: a missing field deserialises to `null` and triggers
   `MethodArgumentNotValidException`; a malformed string causes `HttpMessageNotReadableException` — both map to 400 via
   `CoreExceptionHandlerAdvice`.
6. `SseEmitter(0L)` sets no timeout. `onCompletion` / `onTimeout` shut down the scheduler thread to prevent resource
   leaks.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
