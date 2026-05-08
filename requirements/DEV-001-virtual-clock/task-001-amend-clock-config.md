# Task 001: Amend ClockConfig — replace system default with UTC clock

> **Applied Skill:** `java.instructions.md` — Java best-practices, no-comment convention.
> **Applied Skill:** `springboot.instructions.md` — Spring Boot @Configuration bean patterns.

## 1. Objective

Replace the existing `Clock.systemDefaultZone()` bean in `ClockConfig` with `Clock.systemUTC()`.
This makes the application-wide default clock explicitly UTC, which is the correct baseline before `DevTimeController`'s
`@Primary` override takes effect in `dev`/`test` profiles.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/shared/config/ClockConfig.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

No import changes — `java.time.Clock` is already imported.

**Code to Add/Replace:**

* **Location:** Replace the entire body of the `applicationClock()` method.

```java
@Bean
public Clock applicationClock() {
    return Clock.systemUTC();
}
```

The full file after the change:

```java
package com.github.jenkaby.bikerental.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfig {

    @Bean
    public Clock applicationClock() {
        return Clock.systemUTC();
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests ClockConfig*
```

If no existing test covers `ClockConfig`, verify compilation succeeds:

```bash
./gradlew :service:compileJava
```
