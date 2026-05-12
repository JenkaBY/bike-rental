# Task 005: Add onRentalCancelled Handler to RentalEventListener

> **Applied Skill:** `spring-boot-modulith/SKILL.md` — `@ApplicationModuleListener`,
> asynchronous cross-module event consumers

## 1. Objective

Add a new `onRentalCancelled(RentalCancelled event)` method to `RentalEventListener` that sets
all referenced equipment records to `AVAILABLE` status. Also enable the `@Component` annotation
on the class and activate all existing commented-out `@ApplicationModuleListener` annotations.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/eventlistener/RentalEventListener.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add to the existing import block:

```java
import com.github.jenkaby.bikerental.shared.domain.event.RentalCancelled;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
```

**Step 1 — Enable `@Component` on the class.**

* **Location:** Replace the commented-out `@Component` annotation at the top of the class:

```java
//@Component
public class RentalEventListener {
```

With:

```java
@Component
public class RentalEventListener {
```

**Step 2 — Activate existing `@ApplicationModuleListener` annotations.**

* **Location:** For each commented-out `//    @ApplicationModuleListener` in the existing methods
  (`onRentalStarted(RentalStarted)`, `onRentalStarted(RentalCreated)`, `onRentalCompleted`,
  `onRentalUpdated`), uncomment the annotation:

Replace each `//    @ApplicationModuleListener` with `    @ApplicationModuleListener`.

**Step 3 — Add the new handler method.**

* **Location:** After the closing `}` of the existing `onRentalUpdated` method, before the
  closing `}` of the class.

```java
    @ApplicationModuleListener
    public void onRentalCancelled(RentalCancelled event) {
        log.info("Received RentalCancelled event for equipments {}", event.equipmentIds());
        equipmentRepository.findByIds(event.equipmentIds())
                .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.AVAILABLE.name()));
    }
```

> **Note:** `setStatusForEquipment` and `EquipmentStatus` are already defined/used in the
> existing methods of this class. No additional helper is needed.

## 4. Validation Steps

skip