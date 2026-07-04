<task_file_template>

# Task 010: Extend LifecycleStatus enum with AWAITING_SIGNATURE and DRAFT

> **Applied Skill:** `spring-boot-best-practices` — request DTO enum drives Jackson deserialization + Jakarta
> validation; unknown values become a 400 automatically. `java-best-practices` — enum constants, no comments.

## 1. Objective

Widen the lifecycle request enum so the endpoint accepts the two new target statuses. Update the OpenAPI
description to list them. No controller code changes — `RentalStatus.valueOf(request.status().name())` already
covers the new names.

## 2. File to Modify / Create

* **File Path 1:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/dto/LifecycleStatus.java`
* **File Path 2:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandController.java`
* **Action:** Modify Existing File (both)

## 3. Code Implementation

### Change 3.1 — `LifecycleStatus.java`: replace the whole enum body

* **Location:** Replace the entire enum declaration.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.rental.web.command.dto;

public enum LifecycleStatus {
    DRAFT,
    AWAITING_SIGNATURE,
    ACTIVE,
    CANCELLED
}
```

### Change 3.2 — `RentalCommandController.java`: update the `updateLifecycle` OpenAPI description

* **Location:** In the `@Operation` annotation directly above `updateLifecycle`, the current text reads:

  ```java
      @Operation(summary = "Transition rental lifecycle status",
              description = "Transitions a rental to ACTIVE or CANCELLED status")
  ```

  Replace ONLY those two lines with:

* **Snippet:**

```java
    @Operation(summary = "Transition rental lifecycle status",
            description = "Transitions a rental to AWAITING_SIGNATURE, DRAFT, ACTIVE or CANCELLED status. The response carries the rental version used as the signing fencing token.")
```

> Do NOT change the method body of `updateLifecycle` — `RentalStatus.valueOf(request.status().name())` already
> maps the widened enum. Do NOT touch any other `@Operation` in the controller.

## 4. Validation Steps

Execute the following command to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
