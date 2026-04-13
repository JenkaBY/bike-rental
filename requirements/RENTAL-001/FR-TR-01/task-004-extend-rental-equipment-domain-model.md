# Task 004: Extend `RentalEquipment` Domain Model with `equipmentTypeSlug`

> **Applied Skill:** `.github/skills/spring-boot-data-ddd/SKILL.md` — Aggregate child entities, value objects

## 1. Objective

Add `equipmentType` (String, non-null when populated) to the `RentalEquipment` domain model. Update the static
factory `RentalEquipment.assigned(...)` to accept it so `CreateRentalService` can pass it when building
each rental equipment line from `EquipmentInfo.typeSlug()`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/model/RentalEquipment.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** None (String is already in scope)

**Code to Add/Replace:**

### 3a — Add the new field

* **Location:** Directly **after the `private Long tariffId;` field** (currently the last field before the
  lifecycle methods).

Existing block:

```java
    @Column(name = "tariff_id")
    private Long tariffId;
```

Wait — `RentalEquipment` is a **domain model** (not a JPA entity), so there are no `@Column` annotations here.
The current field list ends with:

```java
    private Money estimatedCost;
    private Money finalCost;
```

Add `equipmentType` directly **after `private Money finalCost;`**:

```java
    private Money estimatedCost;
    private Money finalCost;
    private String equipmentType;
```

### 3b — Update the static factory method

Existing factory method (at the bottom of the class):

```java
    public static RentalEquipment assigned(Long equipmentId, String equipmentUid) {
        return RentalEquipment.builder()
                .equipmentId(equipmentId)
                .equipmentUid(equipmentUid)
                .status(RentalEquipmentStatus.ASSIGNED)
                .build();
    }
```

Replace with:

```java
    public static RentalEquipment assigned(Long equipmentId, String equipmentUid, String equipmentType) {
        return RentalEquipment.builder()
                .equipmentId(equipmentId)
                .equipmentUid(equipmentUid)
                .status(RentalEquipmentStatus.ASSIGNED)
                .equipmentType(equipmentType)
                .build();
    }
```

## 4. Validation Steps

The compiler will flag every call site of the old `assigned(Long, String)` factory with a compilation error.
Those are fixed in Task 007 (`CreateRentalService`) and Task 008 (`UpdateRentalService`).

Run after completing Tasks 006 and 007:

```bash
./gradlew :service:compileJava
```
