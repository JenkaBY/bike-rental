<task_file_template>

# Task 002: Delete Rental.activate(LocalDateTime)

> **Applied Skill:** `java-best-practices` / `spring-boot-data-ddd` — the aggregate keeps a single writer per
> transition; `completeSigning(LocalDateTime)` is now the only path to `ACTIVE` (design.md §2). `canBeActivated()`
> stays exactly as-is because it is still referenced by `prepareForSigning()`. Depends on Task 001 (compiles either
> order, but keep this sequencing for a human reading the checklist).

## 1. Objective

Delete the `activate(LocalDateTime)` method from the `Rental` aggregate. This is the legacy direct-activation code
path being removed per fr.md §2 ("The legacy direct-activation code path ... is removed"). Nothing else in
`Rental.java` changes — `canBeActivated()`, `prepareForSigning()`, and `completeSigning()` are untouched.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/model/Rental.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None to add. Do NOT remove the `RentalNotReadyForActivationException` import — it is still used by
`prepareForSigning()` in the same file.

**Code to Add/Replace:**

* **Location:** Delete the entire `activate(LocalDateTime)` method, located directly above `prepareForSigning()`.
* **Snippet to DELETE (remove this whole block, do not replace it with anything):**

```java
    public void activate(LocalDateTime actualStartTime) {
        this.status.validateTransitionTo(RentalStatus.ACTIVE);

        if (!canBeActivated()) {
            List<String> missingFields = new ArrayList<>();
            if (customerId == null) missingFields.add("customerId");
            if (plannedDuration == null) missingFields.add("plannedDuration");
            if (estimatedCost == null) missingFields.add("estimatedCost");
            if (equipments == null) missingFields.add("equipmentIds");
            throw new RentalNotReadyForActivationException(missingFields);
        }

        this.status = RentalStatus.ACTIVE;
        this.startedAt = actualStartTime; // Actual start time
        this.expectedReturnAt = actualStartTime.plus(this.plannedDuration);

        equipments.forEach(e -> e.activateForRental(this));
        this.updatedAt = Instant.now();
    }

```

After deletion, `canBeActivated()` should be immediately followed by `prepareForSigning()` with a single blank line
between them, exactly like the blank-line spacing used between other methods in this file.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server. This will intentionally still fail to compile
until Task 003/004 remove the callers (`ActivateRentalService`) — that is expected; do not attempt to fix callers in
this task.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
