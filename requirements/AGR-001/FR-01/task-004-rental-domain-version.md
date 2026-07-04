<task_file_template>

# Task 004: Add read-only version field to Rental domain aggregate

> **Applied Skill:** `spring-boot-data-ddd` — aggregate carries the version so the save flow performs
> `merge` (not `persist`); `java-style` — zero inline comments, immutability, Lombok `@Builder` /
> `@Getter`.

## 1. Objective

Add a read-only `Long version` field to the `Rental` aggregate so the JPA version round-trips
(entity → domain → entity) on every load-mutate-save flow. No domain method touches `version`.
The hand-written `RentalBuilder.build()` calls the private all-args constructor explicitly, so its
argument list MUST be updated to include the new field in the exact declaration-order position.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/model/Rental.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

No new imports are needed.

**Code to Add/Replace:**

### Change 3.1 — Declare the `version` field directly below the `id` field

* **Location:** The `id` field is currently declared as:

  ```java
      @Setter
      private Long id;
  ```

  Insert the new field on the line immediately AFTER `private Long id;` and BEFORE the blank line
  preceding `private UUID customerId;`.
* **Snippet:**

```java
    private Long version;
```

After the edit that region must read exactly:

```java
    @Setter
    private Long id;

    private Long version;

    private UUID customerId;
    private List<RentalEquipment> equipments;
```

> The field is exposed via the class-level `@Getter` and included in `@Builder` automatically. Do NOT
> add a `@Setter`.

### Change 3.2 — Update the all-args constructor call inside `RentalBuilder.build()`

Because `version` is now declared as the SECOND field (right after `id`), Lombok's
`@AllArgsConstructor` expects `version` as the SECOND constructor argument. Update the explicit
`new Rental(...)` call accordingly.

* **Location:** Inside the nested `RentalBuilder.build()` method, replace the existing `return new Rental(...)`
  statement.

Replace:

```java
            return new Rental(id, customerId, equipments, status, startedAt, expectedReturnAt,
                    actualReturnAt, plannedDuration, actualDuration, estimatedCost, finalCost,
                    specialTariffId, specialPrice, discountPercent, createdAt, updatedAt);
```

With:

```java
            return new Rental(id, version, customerId, equipments, status, startedAt, expectedReturnAt,
                    actualReturnAt, plannedDuration, actualDuration, estimatedCost, finalCost,
                    specialTariffId, specialPrice, discountPercent, createdAt, updatedAt);
```

> Do NOT modify any other method. `createDraft()` and all other builder usages remain unchanged —
> they simply leave `version` as `null`, which Hibernate populates on load.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
