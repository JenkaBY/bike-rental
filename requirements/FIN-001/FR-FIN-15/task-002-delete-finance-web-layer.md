# Task 002: Delete Finance Module Web-Command Layer

> **Applied Skill:** No dedicated skill file — adheres to hexagonal architecture conventions in `AGENTS.md` (web
> layer = controllers, DTOs, MapStruct mappers).

## 1. Objective

Delete the four source files that together form the `POST /api/payments` handler stack in the Finance module web layer:
the controller, its two DTO records, and its MapStruct mapper. After this task the endpoint is physically removed from
the Spring MVC dispatcher; any call to `POST /api/payments` will return `404 Not Found`.

---

## 2. Files to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/PaymentCommandController.java`
* **Action:** Delete file entirely.

---

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/dto/RecordPaymentRequest.java`
* **Action:** Delete file entirely.

---

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/dto/RecordPaymentResponse.java`
* **Action:** Delete file entirely.

---

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/mapper/PaymentCommandMapper.java`
* **Action:** Delete file entirely.

---

## 3. Code Implementation

Delete all four files listed in Section 2. No replacement files are needed. No other source file in the project
directly imports these classes; their only callers (`PaymentCommandController` → deleted, `FinanceFacadeImpl`
deprecated methods → cleaned in Task 003) are either deleted here or cleaned in the next task.

---

## 4. Validation Steps

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

Expected result: `BUILD SUCCESSFUL` with zero compilation errors in the `service` module.
