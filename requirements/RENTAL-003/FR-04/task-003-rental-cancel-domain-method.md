# Task 003: Add cancel() Domain Method to Rental Aggregate

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — rich domain model; `java.instructions.md`
> — expressive naming, no framework imports in domain

## 1. Objective

Add a `cancel()` method to the `Rental` aggregate that: validates the transition via the state
machine, sets all `RentalEquipment` child records to `RETURNED` status, sets the rental status to
`CANCELLED`, and records `updatedAt`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/model/Rental.java`
* **Action:** Modify Existing File

## 3. Code Implementation

No new imports are required — `RentalStatus`, `RentalEquipmentStatus`, and `Instant` are already
imported in this file.

**Code to Add:**

* **Location:** After the existing `public void addEquipment(RentalEquipment equipment)` method,
  before the `allEquipmentsReturned()` method.

```java
    public void cancel() {
        this.status.validateTransitionTo(RentalStatus.CANCELLED);
        this.equipments.forEach(e -> e.setStatus(RentalEquipmentStatus.RETURNED));
        this.status = RentalStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }
```

## 4. Validation Steps

skip