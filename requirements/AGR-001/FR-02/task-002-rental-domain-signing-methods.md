<task_file_template>

# Task 002: Add signing lifecycle methods and DRAFT guard to Rental aggregate

> **Applied Skill:** `spring-boot-data-ddd` — business invariants live in the aggregate; `java-best-practices`
> — zero inline comments, expressive naming, `List.of()`/mutable-list discipline mirroring the existing
> `activate()` method.

## 1. Objective

Add three lifecycle methods to `Rental`, symmetric with the existing `activate()`: `prepareForSigning()`,
`cancelSigning()`, `completeSigning(LocalDateTime)`; and close the composition-editing hole by adding a
DRAFT-only guard to `updateEquipments(...)`. No new fields. Reuses the existing readiness logic of
`canBeActivated()`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/model/Rental.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

No new imports are needed. `RentalStatus`, `InvalidRentalStatusException`,
`RentalNotReadyForActivationException`, `ArrayList`, `List`, `LocalDateTime`, `Instant` and `Collection` are
already imported in this file.

**Code to Add/Replace:**

### Change 3.1 — Add the DRAFT guard as the FIRST statement of `updateEquipments(...)`

* **Location:** The method currently starts:

  ```java
      public void updateEquipments(Collection<EquipmentInfo> incoming) {
          var incomingIds = incoming.stream()
  ```

  Insert the guard so the method opening becomes exactly:

* **Snippet:**

```java
    public void updateEquipments(Collection<EquipmentInfo> incoming) {
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }
        var incomingIds = incoming.stream()
```

> Do NOT change any other statement inside `updateEquipments`.

### Change 3.2 — Add the three signing methods directly AFTER the `activate(...)` method

* **Location:** The `activate(...)` method ends with:

  ```java
          equipments.forEach(e -> e.activateForRental(this));
          this.updatedAt = Instant.now();
      }
  ```

  Insert the following three methods on the lines immediately AFTER that closing brace `}` and BEFORE the
  `public List<Long> getNewEquipmentIds(...)` method.

* **Snippet:**

```java
    public void prepareForSigning() {
        this.status.validateTransitionTo(RentalStatus.AWAITING_SIGNATURE);

        if (!canBeActivated()) {
            List<String> missingFields = new ArrayList<>();
            if (customerId == null) missingFields.add("customerId");
            if (plannedDuration == null) missingFields.add("plannedDuration");
            if (estimatedCost == null) missingFields.add("estimatedCost");
            if (equipments == null) missingFields.add("equipmentIds");
            throw new RentalNotReadyForActivationException(missingFields);
        }

        this.status = RentalStatus.AWAITING_SIGNATURE;
        this.updatedAt = Instant.now();
    }

    public void cancelSigning() {
        this.status.validateTransitionTo(RentalStatus.DRAFT);
        this.status = RentalStatus.DRAFT;
        this.updatedAt = Instant.now();
    }

    public void completeSigning(LocalDateTime signedAt) {
        if (this.status != RentalStatus.AWAITING_SIGNATURE) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.AWAITING_SIGNATURE);
        }

        this.status = RentalStatus.ACTIVE;
        this.startedAt = signedAt;
        this.expectedReturnAt = signedAt.plus(this.plannedDuration);

        equipments.forEach(e -> e.activateForRental(this));
        this.updatedAt = Instant.now();
    }
```

> Note: `canBeActivated()` returns true only when `status == DRAFT`; `prepareForSigning()` is only reachable
> from DRAFT (the transition validation above guarantees it), so the readiness reuse is correct. Do NOT alter
> `activate()` or `canBeActivated()`.

## 4. Validation Steps

Execute the following command to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
