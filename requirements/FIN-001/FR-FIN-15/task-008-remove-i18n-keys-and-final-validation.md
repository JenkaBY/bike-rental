# Task 008: Remove Deprecated i18n Keys and Final Build Verification

> **Applied Skill:** No dedicated skill file — follows i18n conventions in `AGENTS.md` (error message keys in
> `messages.properties` and `messages_ru.properties`).

## 1. Objective

Remove the two prepayment error message keys from both localisation files (`messages.properties` and
`messages_ru.properties`). Because no handler or exception class references these keys after Tasks 005 and 006,
leaving them would be harmless but constitute dead configuration. After this task, run the full project build as the
final acceptance check for **all** FR-FIN-15 changes.

---

## 2. Files to Modify / Create

* **File Path:** `service/src/main/resources/messages.properties`
* **Action:** Modify existing file — remove two key-value lines.

---

* **File Path:** `service/src/main/resources/messages_ru.properties`
* **Action:** Modify existing file — remove two key-value lines.

---

## 3. Code Implementation

### 3.1 Modify `messages.properties` — remove prepayment error keys

**Location:** The keys appear in the `# Error codes` section, between
`error.rental.update.invalid` and `error.rental.insufficient_funds`.

**Remove these two lines:**

```properties
error.rental.prepayment.required=Prepayment must be received before proceeding
error.rental.prepayment.insufficient=Prepayment amount is insufficient
```

After removal the surrounding block should read:

```properties
error.rental.update.invalid=Invalid rental update request
error.rental.insufficient_funds=Insufficient wallet balance to cover the rental cost
```

---

### 3.2 Modify `messages_ru.properties` — remove prepayment error keys

**Location:** The keys appear in the same relative position as in `messages.properties`
(between `error.rental.update.invalid` and `error.rental.insufficient_funds`).

**Remove these two lines** (the values are Cyrillic-encoded):

```properties
error.rental.prepayment.required=Для продолжения необходимо внести предоплату
error.rental.prepayment.insufficient=Сумма предоплаты недостаточна
```

> **Important:** The file may display the Russian values as mojibake in some editors if the file encoding is not set
> to UTF-8. Use a UTF-8-aware editor or IDE to locate the lines by their keys
> (`error.rental.prepayment.required` and `error.rental.prepayment.insufficient`) and delete both lines.

After removal the surrounding block should read:

```properties
error.rental.update.invalid=<existing Russian value>
error.rental.insufficient_funds=<existing Russian value>
```

---

## 4. Validation Steps

Run the full project build to confirm that **all** FR-FIN-15 changes compile and all retained tests pass:

```bash
./gradlew build "-Dspring.profiles.active=test"
```

Expected result: `BUILD SUCCESSFUL`. Zero compilation errors. All unit tests (`service` module) and, if run,
component tests (`component-test` module) pass.

Additionally, verify no deprecated symbols remain in the codebase by running the following searches (each must return
zero results):

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test" 2>&1 | grep -i "deprecated"
```

Perform a manual text search in the IDE (Find in Files) for the following terms to confirm zero occurrences outside
of version-control history:

- `hasPrepayment`
- `recordPrepayment`
- `recordAdditionalPayment`
- `PrepaymentRequiredException`
- `InsufficientPrepaymentException`
- `RecordPrepaymentUseCase`
- `RecordPrepaymentService`
- `PaymentCommandController`
