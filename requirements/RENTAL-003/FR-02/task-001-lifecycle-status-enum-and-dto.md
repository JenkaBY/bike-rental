# Task 001: Create LifecycleStatus Enum and RentalLifecycleRequest DTO

> **Applied Skill:** `java.instructions.md` — Java Records for DTOs, immutability;
> `springboot.instructions.md` — Bean Validation on request bodies

## 1. Objective

Create the web-layer vocabulary for the lifecycle endpoint: a two-constant `LifecycleStatus` enum
that restricts accepted values to `ACTIVE` and `CANCELLED`, and a `RentalLifecycleRequest` record
that holds the validated request body.

## 2. File to Modify / Create

### File A

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/dto/LifecycleStatus.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.rental.web.command.dto;

public enum LifecycleStatus {
    ACTIVE,
    CANCELLED
}
```

### File B

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/dto/RentalLifecycleRequest.java`
* **Action:** Create New File

**Imports Required:**

```java
import jakarta.validation.constraints.NotNull;
```

```java
package com.github.jenkaby.bikerental.rental.web.command.dto;

import jakarta.validation.constraints.NotNull;

public record RentalLifecycleRequest(
        @NotNull LifecycleStatus status,
        String operatorId
) {
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
