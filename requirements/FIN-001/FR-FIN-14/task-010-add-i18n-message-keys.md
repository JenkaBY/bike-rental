# Task 010: Add i18n Message Keys to `messages.properties` and `messages_ru.properties`

> **Applied Skill:** `java.instructions.md` — keep UI error key naming consistent with the existing
`error.<module>.<code>` pattern used throughout `messages.properties`.

## 1. Objective

Add the two new error code message keys for `INSUFFICIENT_FUNDS` and `HOLD_REQUIRED` to both localisation files so that
the UI can display a human-readable message for each new error code.

## 2. File to Modify / Create (Part A — English)

* **File Path:** `service/src/main/resources/messages.properties`
* **Action:** Modify Existing File

### Code to Add

* **Location:** After the existing `error.rental.prepayment.insufficient=...` line.

**Before:**

```properties
error.rental.prepayment.required=Prepayment must be received before proceeding
error.rental.prepayment.insufficient=Prepayment amount is insufficient
error.rental.activation.not_ready=Rental is missing required fields for activation
```

**After:**

```properties
error.rental.prepayment.required=Prepayment must be received before proceeding
error.rental.prepayment.insufficient=Prepayment amount is insufficient
error.rental.insufficient_funds=Insufficient wallet balance to cover the rental cost
error.rental.hold.required=A fund hold must exist before the rental can be activated
error.rental.activation.not_ready=Rental is missing required fields for activation
```

---

## 3. Validation Steps

```bash
./gradlew :service:compileJava
```
