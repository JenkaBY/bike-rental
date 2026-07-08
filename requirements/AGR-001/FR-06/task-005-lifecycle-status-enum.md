<task_file_template>

# Task 005: Remove ACTIVE from the LifecycleStatus web enum

> **Applied Skill:** `spring-boot-best-practices` — the web-layer enum drives Jackson deserialization of
> `PATCH /api/rentals/{id}/lifecycles`; removing `ACTIVE` makes `{"status":"ACTIVE"}` fail deserialization and return
> the standard 400 validation `ProblemDetail` (design.md §2/§4), with zero handler code changes needed
> (`.claude/rules/error-responses.md` — standard Jakarta/Jackson validation flows through unchanged).

## 1. Objective

Remove the `ACTIVE` constant from `LifecycleStatus`, leaving only `DRAFT`, `AWAITING_SIGNATURE`, `CANCELLED` as valid
values for the lifecycle PATCH endpoint's `status` field.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/dto/LifecycleStatus.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None.

**Code to Add/Replace:**

* **Location:** Whole file.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.rental.web.command.dto;

public enum LifecycleStatus {
    DRAFT,
    AWAITING_SIGNATURE,
    CANCELLED
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server. This will intentionally still fail to compile
until Task 006 removes the `LifecycleStatus.ACTIVE` reference in `RentalCommandControllerTest` — that is expected;
do not attempt to fix the test in this task.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
