# Task 005: Delete Rental Application Layer (Use Case and Service)

> **Applied Skill:** No dedicated skill file — follows hexagonal architecture conventions in `AGENTS.md`
> (application use-case interfaces and service implementations).

## 1. Objective

Delete `RecordPrepaymentUseCase` and `RecordPrepaymentService` from the Rental module application layer. After
Task 004, `RentalCommandController` no longer injects or calls `RecordPrepaymentUseCase`, so these two files have
zero callers and can be deleted without further source changes.

---

## 2. Files to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/RecordPrepaymentUseCase.java`
* **Action:** Delete file entirely.

---

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/RecordPrepaymentService.java`
* **Action:** Delete file entirely.

---

## 3. Code Implementation

Delete both files. No replacement files or additional source changes are required in this task.

---

## 4. Validation Steps

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

Expected result: `BUILD SUCCESSFUL` with zero compilation errors.
