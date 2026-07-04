<task_file_template>

# Task 006: Update RentalResponseTransformer for the new version component

> **Applied Skill:** `spring-boot-java-cucumber` — DataTable entry transformers live in `transformer/`
> and produce already-transformed objects; `java-style` — zero inline comments.

## 1. Objective

Task-005 added a `version` component to `RentalResponse` as its second component. This transformer is
the ONLY code that builds `RentalResponse` positionally (`new RentalResponse(...)`), so its call must
insert `version` in the second position to keep the component-test module compiling. The transformer
reads an optional `version` column from the DataTable (defaults to `null` when absent).

## 2. File to Modify / Create

* **File Path:** `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/RentalResponseTransformer.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

No new imports are needed (`DataTableHelper` is in the same package).

**Code to Add/Replace:**

### Change 3.1 — Parse the optional `version` column

* **Location:** Immediately AFTER this existing line inside the `rentalResponse` method:

  ```java
        var finalCost = DataTableHelper.toBigDecimal(entry, "totalCost");
  ```

  (and before the `if (DataTableHelper.toBigDecimal(entry, "finalCost") != null) {` block).
* **Snippet:**

```java
        var version = DataTableHelper.toLong(entry, "version");
```

### Change 3.2 — Insert `version` into the positional constructor call

* **Location:** The `return new RentalResponse(...)` statement at the end of the method.

Replace:

```java
        return new RentalResponse(
                id,
                customerId,
                new ArrayList<>(),// will be populated later on
                status,
                startedAt,
                expectedReturnAt,
                actualReturnAt,
                plannedDurationMinutes,
                actualDurationMinutes,
                estimatedCost,
                specialPrice,
                discountPercent,
                finalCost);
```

With:

```java
        return new RentalResponse(
                id,
                version,
                customerId,
                new ArrayList<>(),
                status,
                startedAt,
                expectedReturnAt,
                actualReturnAt,
                plannedDurationMinutes,
                actualDurationMinutes,
                estimatedCost,
                specialPrice,
                discountPercent,
                finalCost);
```

> Note: the existing inline comment `// will be populated later on` is removed to comply with the
> zero-inline-comment rule.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :component-test:compileTestJava "-Dspring.profiles.active=test"
```

</task_file_template>
