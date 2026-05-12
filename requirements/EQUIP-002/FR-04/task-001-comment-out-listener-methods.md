# Task 001: Comment Out Rental Event Listener Methods in `RentalEventListener`

> **Applied Skill:** `spring-boot-modulith/SKILL.md` — Module boundary enforcement; removing equipment → rental runtime
> dependency; `@ApplicationModuleListener` removal.

## 1. Objective

Comment out all four `@ApplicationModuleListener`-annotated methods in `RentalEventListener` so that
the `equipment` module has zero runtime references to any `rental.*` type. The class and its
`@Component` annotation are preserved for easy re-enablement. Each disabled block must be preceded
by a `// TODO: [REFACTORING]` comment as documented in the FR.

After this change, `equipments.status_slug` will no longer be updated by rental lifecycle events
(accepted transitional state — see EQUIP-002/FR-04/fr.md).

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/eventlistener/RentalEventListener.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** No new imports. The existing event-type imports (`RentalCreated`,
`RentalStarted`, `RentalCompleted`, `RentalUpdated`) are commented out together with the methods
that reference them, removing the cross-module import dependency.

**Code to Add/Replace:**

Replace the **entire file content** with the following. All four listener methods and their
referencing imports are commented out. The class structure, constructor, private helper, and inner
enums are preserved.

```java
package com.github.jenkaby.bikerental.equipment.infrastructure.eventlistener;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
// TODO: [REFACTORING] Imports below are disabled while rental lifecycle listeners are commented out.
//       Re-enable after full migration of availability tracking to rental module.
//       Ticket: EQUIP-002 / RENTAL-002 follow-up.
//import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
//import com.github.jenkaby.bikerental.shared.domain.event.RentalCreated;
//import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
//import com.github.jenkaby.bikerental.shared.domain.event.RentalUpdated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class RentalEventListener {

    private final EquipmentRepository equipmentRepository;
    private final UpdateEquipmentUseCase updateEquipmentUseCase;
    private final EquipmentCommandToDomainMapper equipmentCommandToDomainMapper;

    public RentalEventListener(
            EquipmentRepository equipmentRepository,
            UpdateEquipmentUseCase updateEquipmentUseCase,
            EquipmentCommandToDomainMapper equipmentCommandToDomainMapper) {
        this.equipmentRepository = equipmentRepository;
        this.updateEquipmentUseCase = updateEquipmentUseCase;
        this.equipmentCommandToDomainMapper = equipmentCommandToDomainMapper;
    }

    // TODO: [REFACTORING] Re-enable after full migration of availability tracking to rental module.
    //       Ticket: EQUIP-002 / RENTAL-002 follow-up.
    //       equipments.status_slug is stale while this listener is disabled.
//    @ApplicationModuleListener
//    public void onRentalStarted(RentalStarted event) {
//        log.info("Received RentalStarted event for equipments {}", event.equipmentIds());
//        equipmentRepository.findByIds(event.equipmentIds())
//                .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.RENTED.name()));
//    }

    // TODO: [REFACTORING] Re-enable after full migration of availability tracking to rental module.
    //       Ticket: EQUIP-002 / RENTAL-002 follow-up.
//    @ApplicationModuleListener
//    public void onRentalStarted(RentalCreated event) {
//        log.info("Received RentalCreated event {}", event);
//        equipmentRepository.findByIds(event.equipmentIds())
//                .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.RESERVED.name()));
//    }

    // TODO: [REFACTORING] Re-enable after full migration of availability tracking to rental module.
    //       Ticket: EQUIP-002 / RENTAL-002 follow-up.
//    @ApplicationModuleListener
//    public void onRentalCompleted(RentalCompleted event) {
//        log.info("Received RentalCompleted event for equipments {}", event.equipmentIds());
//        equipmentRepository.findByIds(event.returnedEquipmentIds())
//                .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.AVAILABLE.name()));
//    }

    // TODO: [REFACTORING] Re-enable after full migration of availability tracking to rental module.
    //       Ticket: EQUIP-002 / RENTAL-002 follow-up.
//    @ApplicationModuleListener
//    public void onRentalUpdated(RentalUpdated event) {
//        log.info("Received RentalUpdated event {}", event);
//        if (CollectionUtils.isEmpty(event.currentState().equipmentIds()) && CollectionUtils.isEmpty(event.previousState().equipmentIds())) {
//            return;
//        }
//        if (RentalStatus.isCancelled(event.currentState().rentalStatus())) {
//            var ids = Stream.of(event.currentState().equipmentIds(), event.previousState().equipmentIds())
//                    .flatMap(Collection::stream)
//                    .collect(Collectors.toSet());
//            equipmentRepository.findByIds(ids)
//                    .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.AVAILABLE.name()));
//        }
//        // TODO Verify this
//        if (RentalStatus.isDraft(event.currentState().rentalStatus())) {
//            if (!CollectionUtils.isEmpty(event.previousState().equipmentIds())) {
//                equipmentRepository.findByIds(event.previousState().equipmentIds())
//                        .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.AVAILABLE.name()));
//            }
//            if (!CollectionUtils.isEmpty(event.currentState().equipmentIds())) {
//                equipmentRepository.findByIds(event.currentState().equipmentIds())
//                        .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.RESERVED.name()));
//            }
//        }
//    }

    private void setStatusForEquipment(Equipment equipment, String targetStatus) {
        try {
            if (targetStatus.equals(equipment.getStatusSlug())) {
                log.debug("Equipment {} already in {} status, skipping", equipment.getId(), targetStatus);
                return;
            }
            var command = equipmentCommandToDomainMapper.toUpdateCommand(equipment, targetStatus);
            updateEquipmentUseCase.execute(command);
            log.info("Successfully changed equipment {} status to {}", equipment.getId(), targetStatus);

        } catch (Exception e) {
            log.error("Failed to update equipment {} status to {}: {}", equipment.getId(), targetStatus, e.getMessage(), e);
        }
    }

    enum RentalStatus {
        DRAFT,
        ACTIVE,
        CANCELLED;

        public static boolean isCancelled(String status) {
            return CANCELLED.name().equals(status);
        }

        public static boolean isDraft(String status) {
            return DRAFT.name().equals(status);
        }
    }

    enum EquipmentStatus {
        AVAILABLE,
        RESERVED,
        RENTED
    }
}
```

**Note on unused imports:** The Java compiler will warn about the unused `CollectionUtils`, `Collection`,
`Collectors`, and `Stream` imports (they are referenced only inside commented-out code). Remove those
four import lines as well:

Locate and delete these lines:

```java
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
```

The final import section should be:

```java
import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
```

## 4. Validation Steps

Compile check:

```bash
./gradlew :service:compileJava
```

Verify no `rental.*` type is referenced in the equipment module:

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests "com.github.jenkaby.bikerental.BikeRentalApplicationTest"
```

`BikeRentalApplicationTest` runs Spring Modulith's module structure verification at context load;
it will fail if any equipment → rental boundary violation is detected.
