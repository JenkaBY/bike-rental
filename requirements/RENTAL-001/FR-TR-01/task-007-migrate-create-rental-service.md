# Task 007: Migrate `CreateRentalService` from TariffFacade V1 to TariffV2Facade

> **Applied Skill:** `.github/skills/spring-boot-data-ddd/SKILL.md` — Transactional service, one aggregate per
> transaction

## 1. Objective

Replace all calls to the deprecated `TariffFacade` (V1) inside `CreateRentalService` with a single batch call
to `TariffV2Facade.calculateRentalCost(RentalCostCalculationCommand)`. Delegate the assembly of
`RentalCostCalculationCommand` to a new `RentalCostCommandMapper`. Persist the V2 pricing overrides
(`specialTariffId`, `specialPrice`, `discountPercent`) on the `Rental` aggregate. Populate
`RentalEquipment.estimatedCost` per item from `RentalCostCalculationResult.equipmentBreakdowns()`. Pass
`RentalCostCalculationResult.totalCost()` to `FinanceFacade.holdFunds`. Call the updated
`RentalEquipment.assigned(id, uid, equipmentType)` factory.

## 2. Files to Modify / Create

---

### File A: `EquipmentCostItemMapper.java` — New application-layer mapper

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/mapper/EquipmentCostItemMapper.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.tariff.EquipmentCostItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface EquipmentCostItemMapper {

    @Mapping(target = "equipmentType", source = "typeSlug")
    EquipmentCostItem toEquipmentCostItem(EquipmentInfo equipmentInfo);

    List<EquipmentCostItem> toEquipmentCostItems(List<EquipmentInfo> equipments);
}
```

---

### File B: `RentalCostCommandMapper.java` — New application-layer mapper

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/mapper/RentalCostCommandMapper.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationCommand;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Mapper(uses = {EquipmentCostItemMapper.class})
public abstract class RentalCostCommandMapper {

    protected Clock clock;
    protected EquipmentCostItemMapper equipmentCostItemMapper;

    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Autowired
    public void setEquipmentCostItemMapper(EquipmentCostItemMapper equipmentCostItemMapper) {
        this.equipmentCostItemMapper = equipmentCostItemMapper;
    }

    public RentalCostCalculationCommand toCommand(
            CreateRentalUseCase.CreateRentalCommand command,
            List<EquipmentInfo> equipments) {
        var costItems = equipmentCostItemMapper.toEquipmentCostItems(equipments);
        if (command.specialTariffId() != null) {
            return new RentalCostCalculationCommand(
                    costItems,
                    command.duration(),
                    null,
                    null,
                    command.specialTariffId(),
                    command.specialPrice(),
                    LocalDate.now(clock));
        }
        return new RentalCostCalculationCommand(
                costItems,
                command.duration(),
                null,
                command.discountPercent(),
                null,
                null,
                LocalDate.now(clock));
    }

    public RentalCostCalculationCommand toCommand(
            Rental rental,
            List<EquipmentInfo> equipments) {
        var costItems = equipmentCostItemMapper.toEquipmentCostItems(equipments);
        if (rental.getSpecialTariffId() != null) {
            return new RentalCostCalculationCommand(
                    costItems,
                    rental.getPlannedDuration(),
                    null,
                    null,
                    rental.getSpecialTariffId(),
                    rental.getSpecialPrice(),
                    LocalDate.now(clock));
        }
        return new RentalCostCalculationCommand(
                costItems,
                rental.getPlannedDuration(),
                null,
                rental.getDiscountPercent(),
                null,
                null,
                LocalDate.now(clock));
    }
}
```

---

### File B: `CreateRentalService.java` — Migrate to V2

## 3. Code Implementation

### 3a — Replace imports

Remove:

```java
import com.github.jenkaby.bikerental.tariff.TariffFacade;
import com.github.jenkaby.bikerental.tariff.TariffInfo;
```

Add:

```java
import com.github.jenkaby.bikerental.rental.application.mapper.RentalCostCommandMapper;
import com.github.jenkaby.bikerental.tariff.TariffV2Facade;
```

Remove any imports that are no longer needed after the refactor:

```java
import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent; // only needed inside mapper now
import com.github.jenkaby.bikerental.tariff.EquipmentCostItem;              // only needed inside mapper now
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationCommand;   // only needed inside mapper now
```

---

### 3b — Replace the `TariffFacade` field with `TariffV2Facade`

Find the existing constructor and field block:

```java
    private final RentalRepository repository;
    private final CustomerFacade customerFacade;
    private final EquipmentFacade equipmentFacade;
    private final TariffFacade tariffFacade;
    private final EventPublisher eventPublisher;
    private final RentalEventMapper eventMapper;
    private final Clock clock;
    private final RequestedEquipmentValidator validator;
    private final FinanceFacade financeFacade;

    CreateRentalService(
            RentalRepository repository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            TariffFacade tariffFacade,
            EventPublisher eventPublisher,
            RentalEventMapper eventMapper,
            Clock clock,
            RequestedEquipmentValidator validator,
            FinanceFacade financeFacade) {
        this.repository = repository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.tariffFacade = tariffFacade;
        this.eventPublisher = eventPublisher;
        this.eventMapper = eventMapper;
        this.clock = clock;
        this.validator = validator;
        this.financeFacade = financeFacade;
    }
```

Replace with:

```java
    private final RentalRepository repository;
    private final CustomerFacade customerFacade;
    private final EquipmentFacade equipmentFacade;
    private final TariffV2Facade tariffV2Facade;
    private final EventPublisher eventPublisher;
    private final RentalEventMapper eventMapper;
    private final RentalCostCommandMapper costCommandMapper;
    private final Clock clock;
    private final RequestedEquipmentValidator validator;
    private final FinanceFacade financeFacade;

    CreateRentalService(
            RentalRepository repository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            TariffV2Facade tariffV2Facade,
            EventPublisher eventPublisher,
            RentalEventMapper eventMapper,
            RentalCostCommandMapper costCommandMapper,
            Clock clock,
            RequestedEquipmentValidator validator,
            FinanceFacade financeFacade) {
        this.repository = repository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.tariffV2Facade = tariffV2Facade;
        this.eventPublisher = eventPublisher;
        this.eventMapper = eventMapper;
        this.costCommandMapper = costCommandMapper;
        this.clock = clock;
        this.validator = validator;
        this.financeFacade = financeFacade;
    }
```

---

### 3c — Replace the `execute(CreateRentalCommand)` method body

Find the entire `execute(CreateRentalCommand command)` implementation starting at:

```java
    @Override
    @Transactional
    public Rental execute(CreateRentalCommand command) {
        customerFacade.findById(command.customerId())
                .orElseThrow(() -> new ReferenceNotFoundException("Customer", command.customerId().toString()));
```

Replace the entire method (from `@Override` through the final `return saved;` and the closing brace before
`execute(CreateDraftCommand)`) with:

```java
    @Override
    @Transactional
    public Rental execute(CreateRentalCommand command) {
        customerFacade.findById(command.customerId())
                .orElseThrow(() -> new ReferenceNotFoundException("Customer", command.customerId().toString()));

        if (CollectionUtils.isEmpty(command.equipmentIds())) {
            throw new IllegalArgumentException("At least one equipmentId must be provided");
        }

        var equipments = equipmentFacade.findByIds(command.equipmentIds());
        validator.validateSize(command.equipmentIds(), equipments);
        validator.validateAvailability(equipments);

        var costCommand = costCommandMapper.toCommand(command, equipments);
        var costResult = tariffV2Facade.calculateRentalCost(costCommand);
        var breakdowns = costResult.equipmentBreakdowns();

        Rental rental = Rental.builder()
                .status(RentalStatus.DRAFT)
                .customerId(command.customerId())
                .createdAt(Instant.now())
                .equipments(new ArrayList<>())
                .plannedDuration(command.duration())
                .specialTariffId(command.specialTariffId())
                .specialPrice(command.specialPrice())
                .discountPercent(command.discountPercent())
                .build();

        for (int i = 0; i < equipments.size(); i++) {
            var equipment = equipments.get(i);
            var rentalEquipment = RentalEquipment.assigned(
                    equipment.id(),
                    equipment.uid(),
                    equipment.typeSlug());
            rentalEquipment.setEstimatedCost(breakdowns.get(i).itemCost());
            rental.addEquipment(rentalEquipment);
        }

        Rental saved = repository.save(rental);

        if (saved.getEstimatedCost().isPositive()) {
            var holdInfo = financeFacade.holdFunds(
                    new CustomerRef(saved.getCustomerId()),
                    saved.toRentalRef(),
                    costResult.totalCost(),
                    command.operatorId());
            log.info("Funds held for rental {}: transactionId={}, heldAt={}",
                    saved.getId(), holdInfo.transactionRef().id(), holdInfo.recordedAt());
        }

        RentalCreated event = eventMapper.toRentalCreated(saved);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
        return saved;
    }
```

---

### 3d — Delete the private helper method `autoSelectTariff`

Remove the entire method:

```java
    private Long autoSelectTariff(EquipmentInfo equipment, Duration duration) {
        TariffInfo selectedTariff = tariffFacade.selectTariff(
                equipment.typeSlug(),
                duration,
                LocalDate.now(clock)
        );
        return selectedTariff.id();
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
