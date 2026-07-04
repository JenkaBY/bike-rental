<task_file_template>

# Task 005: Expose version in RentalResponse DTO

> **Applied Skill:** `java-style` — records for DTOs, zero inline comments; `mapstruct-hexagonal` —
> MapStruct maps `version ↔ version` by name (no mapper change needed).

## 1. Objective

Add a `Long version` component to the `RentalResponse` record so every rental JSON response exposes
the optimistic-lock counter as a fencing token. Place it after `id` for readability.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/web/query/dto/RentalResponse.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

No new imports are needed (`io.swagger.v3.oas.annotations.media.Schema`,
`jakarta.validation.constraints.NotNull` and `java.lang.Long` are already available).

**Code to Add/Replace:**

* **Location:** Add the new component as the SECOND record component, immediately AFTER the `id`
  component and BEFORE the `customerId` component.

Replace:

```java
        @Schema(description = "Rental ID", example = "1") @NotNull Long id,
        @Schema(description = "Customer UUID") @NotNull UUID customerId,
```

With:

```java
        @Schema(description = "Rental ID", example = "1") @NotNull Long id,
        @Schema(description = "Optimistic-lock version, starts at 0", example = "0") @NotNull Long version,
        @Schema(description = "Customer UUID") @NotNull UUID customerId,
```

> No change to `RentalQueryMapper` is required: after task-004 the domain `Rental` carries a
> `version` field, so MapStruct maps `Rental.version → RentalResponse.version` automatically by name.
> If the build reports an unmapped `version` target, STOP and re-check that task-004 was applied.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
