# Task 008: Migrate `UpdateRentalService` from TariffFacade V1 to TariffV2Facade

> **Applied Skill:** `.github/skills/spring-boot-data-ddd/SKILL.md` — Transactional service, aggregate update

## 1. Objective

Replace all calls to the deprecated `TariffFacade` (V1) inside `UpdateRentalService` with a single batch call
to `TariffV2Facade.calculateRentalCost(RentalCostCalculationCommand)`, using the pricing overrides already stored
on the loaded `Rental` aggregate. Update the `RentalEquipment.assigned(...)` call to pass `equipmentType`.
Remove the `tariffFacade` field and `selectTariff` helper method entirely.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/UpdateRentalService.java`
* **Action:** Modify Existing File

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

Remove the following imports — they are now encapsulated inside `RentalCostCommandMapper`:

```java
import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
import com.github.jenkaby.bikerental.tariff.EquipmentCostItem;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationCommand;
```

---

### 3b — Replace the `TariffFacade` field and constructor injection

Find the existing field and constructor block:

```java
    private final RentalRepository rentalRepository;
    private final CustomerFacade customerFacade;
    private final EquipmentFacade equipmentFacade;
    private final TariffFacade tariffFacade;
    private final FinanceFacade financeFacade;
    private final EventPublisher eventPublisher;
    private final Clock clock;
    private final RentalEventMapper eventMapper;
    private final PatchValueParser valueParser;
    private final RequestedEquipmentValidator validator;

    UpdateRentalService(
            RentalRepository rentalRepository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            TariffFacade tariffFacade,
            FinanceFacade financeFacade,
            EventPublisher eventPublisher,
            Clock clock,
            RentalEventMapper eventMapper,
            PatchValueParser valueParser,
            RequestedEquipmentValidator validator) {
        this.rentalRepository = rentalRepository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.tariffFacade = tariffFacade;
        this.financeFacade = financeFacade;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.eventMapper = eventMapper;
        this.valueParser = valueParser;
        this.validator = validator;
    }
```

Replace with:

```java
    private final RentalRepository rentalRepository;
    private final CustomerFacade customerFacade;
    private final EquipmentFacade equipmentFacade;
    private final TariffV2Facade tariffV2Facade;
    private final FinanceFacade financeFacade;
    private final EventPublisher eventPublisher;
    private final Clock clock;
    private final RentalEventMapper eventMapper;
    private final RentalCostCommandMapper costCommandMapper;
    private final PatchValueParser valueParser;
    private final RequestedEquipmentValidator validator;

    UpdateRentalService(
            RentalRepository rentalRepository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            TariffV2Facade tariffV2Facade,
            FinanceFacade financeFacade,
            EventPublisher eventPublisher,
            Clock clock,
            RentalEventMapper eventMapper,
            RentalCostCommandMapper costCommandMapper,
            PatchValueParser valueParser,
            RequestedEquipmentValidator validator) {
        this.rentalRepository = rentalRepository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.tariffV2Facade = tariffV2Facade;
        this.financeFacade = financeFacade;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.eventMapper = eventMapper;
        this.costCommandMapper = costCommandMapper;
        this.valueParser = valueParser;
        this.validator = validator;
    }
```

---

### 3c — Replace the `equipmentIds` patch branch

Locate the entire `if (patch.containsKey("equipmentIds"))` block:

```java
        if (patch.containsKey("equipmentIds")) {
            List<Long> equipmentIds = valueParser.parseListOfLong(patch.get("equipmentIds"));
            List<EquipmentInfo> foundEquipments = equipmentFacade.findByIds(equipmentIds);
            validator.validateSize(equipmentIds, foundEquipments);
            var alreadyReservedOrRented = previousState.equipmentIds();
            var beingReserved = foundEquipments.stream()
                    .filter(e -> !alreadyReservedOrRented.contains(e.id()))
                    .toList();
            validator.validateAvailability(beingReserved);
            equipments.addAll(foundEquipments);

            rental.clearEquipmentRentals();
            for (var equipment : equipments) {
                RentalEquipment rentalEquipment = RentalEquipment.assigned(equipment.id(), equipment.uid());

                TariffInfo tariff = selectTariff(rental, equipment);
                var cost = tariffFacade.calculateRentalCost(tariff.id(), rental.getPlannedDuration());
                rentalEquipment.setTariffId(tariff.id());
                rentalEquipment.setEstimatedCost(cost.totalCost());
                rental.addEquipment(rentalEquipment);
            }
        }
```

Replace with:

```java
        if (patch.containsKey("equipmentIds")) {
            List<Long> equipmentIds = valueParser.parseListOfLong(patch.get("equipmentIds"));
            List<EquipmentInfo> foundEquipments = equipmentFacade.findByIds(equipmentIds);
            validator.validateSize(equipmentIds, foundEquipments);
            var alreadyReservedOrRented = previousState.equipmentIds();
            var beingReserved = foundEquipments.stream()
                    .filter(e -> !alreadyReservedOrRented.contains(e.id()))
                    .toList();
            validator.validateAvailability(beingReserved);
            equipments.addAll(foundEquipments);

            if (rental.getPlannedDuration() == null) {
                throw new InvalidRentalPlannedDurationException(rental.getId());
            }

            var costCommand = costCommandMapper.toCommand(rental, foundEquipments);
            var costResult = tariffV2Facade.calculateRentalCost(costCommand);
            var breakdowns = costResult.equipmentBreakdowns();

            rental.clearEquipmentRentals();
            for (int i = 0; i < foundEquipments.size(); i++) {
                var equipment = foundEquipments.get(i);
                RentalEquipment rentalEquipment = RentalEquipment.assigned(
                        equipment.id(),
                        equipment.uid(),
                        equipment.typeSlug());
                rentalEquipment.setEstimatedCost(breakdowns.get(i).itemCost());
                rental.addEquipment(rentalEquipment);
            }
        }
```

---

### 3d — Remove the `tariffId` patch branch

The `if (patch.containsKey("tariffId"))` block uses `TariffFacade.findById` and is rendered obsolete by V2.
Remove it entirely:

```java
        if (patch.containsKey("tariffId")) {
            // Handle tariffId update (manual override) for special cases. Have no idea how to implement this at the moment.
            Long tariffId = valueParser.parseLong(patch.get("tariffId"));
            tariffFacade.findById(tariffId)
                    .orElseThrow(() -> new ReferenceNotFoundException("Tariff", tariffId.toString()));
            rental.selectTariff(tariffId);
        }
```

---

### 3e — Delete the private helper `selectTariff`

Remove the entire private method at the bottom of the class:

```java
    private TariffInfo selectTariff(Rental rental, EquipmentInfo equipment) {
        var plannedDuration = rental.getPlannedDuration();
        if (plannedDuration == null) {
            throw new InvalidRentalPlannedDurationException(rental.getId());
        }
        return tariffFacade.selectTariff(
                equipment.typeSlug(),
                plannedDuration,
                LocalDate.now(clock));
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
