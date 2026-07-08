<task_file_template>

# Task 003: Delete ActivateRentalService and ActivateRentalUseCase

> **Applied Skill:** `spring-boot-modulith` — removes the now-orphaned use case interface and its single
> implementation together (a port with no callers left is dead code, not just an unused method). No unit test
> references these classes (this project covers services via component tests, not unit tests — see
> `.claude/rules/unit-tests.md`). Depends on Task 002 (the domain method `Rental.activate` these classes called is
> already deleted).

## 1. Objective

Delete the legacy direct-activation use case interface and its Spring `@Service` implementation entirely. Activation
is now exclusively performed by `CompleteSigningService` via `RentalSigningFacade` (agreement module, FR-05).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ActivateRentalService.java`
* **Action:** Delete File (remove it from the filesystem entirely)

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/ActivateRentalUseCase.java`
* **Action:** Delete File (remove it from the filesystem entirely)

## 3. Code Implementation

No code to add. Delete both files listed above completely. Do not leave empty files behind.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server. This will intentionally still fail to compile
until Task 004 removes the `RentalLifecycleService` dependency on `ActivateRentalUseCase` — that is expected; do not
attempt to fix `RentalLifecycleService` in this task.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
