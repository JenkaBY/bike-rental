# Task 002: Create `GetAvailableForRentEquipmentsService` Use-Case Implementation

> **Applied Skill:** `d:\Projects\private\bikerent\.github\instructions\springboot.instructions.md` — Application
> service implementing the use-case interface; constructor injection; cross-module boundary respected (rental →
> `EquipmentFacade` only). `d:\Projects\private\bikerent\.github\instructions\java.instructions.md` — In-memory
> two-phase filter + best-effort pagination using the domain `Page` / `PageRequest` types.

## 1. Objective

Create `GetAvailableForRentEquipmentsService` in `rental/application/service/`. It implements
`GetAvailableForRentEquipmentsUseCase` using the two-phase filter pattern:

1. Call `EquipmentFacade.getEquipmentsByConditions(Set.of(Condition.GOOD), filter)`.
2. If empty → return `Page.empty(pageRequest)` immediately (skip steps 3–5).
3. Extract IDs, call `EquipmentAvailabilityService.getUnavailableIds(ids)`.
4. Filter out unavailable IDs.
5. Map the available `EquipmentInfo` list to `AvailableForRentalEquipment` via `AvailableForRentalEquipmentMapper`,
   apply in-memory pagination, return `Page<AvailableForRentalEquipment>`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/GetAvailableForRentEquipmentsService.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** See snippet below.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.equipment.EquipmentSearchFilter;
import com.github.jenkaby.bikerental.rental.application.mapper.AvailableForRentalEquipmentMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.GetAvailableForRentEquipmentsUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.AvailableForRentalEquipment;
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GetAvailableForRentEquipmentsService implements GetAvailableForRentEquipmentsUseCase {

    private final EquipmentFacade equipmentFacade;
    private final EquipmentAvailabilityService equipmentAvailabilityService;
    private final AvailableForRentalEquipmentMapper availableForRentalEquipmentMapper;

    public GetAvailableForRentEquipmentsService(
            EquipmentFacade equipmentFacade,
            EquipmentAvailabilityService equipmentAvailabilityService,
            AvailableForRentalEquipmentMapper availableForRentalEquipmentMapper) {
        this.equipmentFacade = equipmentFacade;
        this.equipmentAvailabilityService = equipmentAvailabilityService;
        this.availableForRentalEquipmentMapper = availableForRentalEquipmentMapper;
    }

    @Override
    public Page<AvailableForRentalEquipment> getAvailableEquipments(EquipmentSearchFilter filter, PageRequest pageRequest) {
        List<EquipmentInfo> candidates = equipmentFacade.getEquipmentsByConditions(Set.of(Condition.GOOD), filter);

        if (candidates.isEmpty()) {
            return Page.empty(pageRequest);
        }

        Set<Long> candidateIds = candidates.stream()
                .map(EquipmentInfo::id)
                .collect(Collectors.toSet());

        Set<Long> unavailableIds = equipmentAvailabilityService.getUnavailableIds(candidateIds);

        List<AvailableForRentalEquipment> available = candidates.stream()
                .filter(eq -> !unavailableIds.contains(eq.id()))
                .map(availableForRentalEquipmentMapper::toDomain)
                .toList();

        int offset = pageRequest.offset();
        int total = available.size();
        List<AvailableForRentalEquipment> pageContent = available.subList(
                Math.min(offset, total),
                Math.min(offset + pageRequest.limit(), total)
        );

        return new Page<>(pageContent, total, pageRequest);
    }
}
```

> **Key implementation notes:**
> - `AvailableForRentalEquipmentMapper` is constructor-injected; MapStruct generates the Spring bean automatically.
> - The filter step (remove unavailable IDs) works on `EquipmentInfo` (which has `.id()`), then the `.map()` call
    > converts the filtered stream to `AvailableForRentalEquipment` before `.toList()`.
> - `Condition.GOOD` is the sole condition filter passed to the equipment Facade — no other conditions.
> - In-memory pagination uses `pageRequest.offset()` and `pageRequest.limit()` (both defined on `PageRequest`).
> - `Math.min(offset, total)` guards against `offset > total` when the page is beyond the last item.
> - `Page.empty(pageRequest)` is the canonical early-exit for zero candidates (defined as a static factory on `Page`).
> - `total` in the `Page` constructor is the filtered count (GOOD + available), not the raw equipment count.
> - The `Condition` enum is in `com.github.jenkaby.bikerental.shared.domain.model.Condition`.

## 4. Validation Steps

Execute the following commands to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava
```
