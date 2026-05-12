# Task 002: Remove holdFunds from CreateRentalService, Delegate to ActivateRentalUseCase

> **Applied Skill:** `springboot.instructions.md` — single-responsibility services;
> `java.instructions.md` — remove dead code

## 1. Objective

Remove the inline `holdFunds` block from `CreateRentalService.execute(CreateRentalCommand)` and
replace it with a call to `ActivateRentalUseCase`. This makes hold placement the exclusive
responsibility of the activation use case.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CreateRentalService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Step 1 — Add `ActivateRentalUseCase` to the constructor.**

* **Location:** In the class field declarations, after `private final RentalEquipmentFactory rentalEquipmentFactory;`.

Add the new field:

```java
    private final ActivateRentalUseCase activateRentalUseCase;
```

* **Location:** In the constructor parameter list, add `ActivateRentalUseCase activateRentalUseCase`
  as the last parameter, and assign it in the constructor body.

Replace the existing constructor signature and body with:

```java
    CreateRentalService(
            RentalRepository repository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            TariffV2Facade tariffV2Facade,
            EventPublisher eventPublisher,
            Clock clock,
            RentalEventMapper eventMapper,
            RentalEquipmentFactory rentalEquipmentFactory,
            ActivateRentalUseCase activateRentalUseCase) {
        this.repository = repository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.tariffV2Facade = tariffV2Facade;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.eventMapper = eventMapper;
        this.rentalEquipmentFactory = rentalEquipmentFactory;
        this.activateRentalUseCase = activateRentalUseCase;
    }
```

> **Note:** Verify the exact existing constructor parameter names against the current source file
> before replacing. Only add `activateRentalUseCase`; do not change existing parameter names.

**Step 2 — Remove `FinanceFacade` from the constructor and field.**

* **Location:** Remove `private final FinanceFacade financeFacade;` field declaration.
* Remove `FinanceFacade financeFacade` from the constructor parameter list.
* Remove `this.financeFacade = financeFacade;` assignment in the constructor body.
* Remove the `import com.github.jenkaby.bikerental.finance.FinanceFacade;` import statement.

**Step 3 — Replace the `holdFunds` block with delegation to `ActivateRentalUseCase`.**

* **Location:** In `execute(CreateRentalCommand command)`, locate the block:

```java
        Rental saved = repository.save(rental);

        if (saved.getEstimatedCost().isPositive()) {
            var holdInfo = financeFacade.holdFunds(
                    new CustomerRef(saved.getCustomerId()),
                    saved.toRentalRef(),
                    rental.getEstimatedCost(),
                    command.operatorId());
            log.info("Funds held for rental {}: transactionId={}, heldAt={}",
                    saved.getId(), holdInfo.transactionRef().id(), holdInfo.recordedAt());
        }

        RentalCreated event = eventMapper.toRentalCreated(saved);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
        return saved;
```

Replace it with:

```java
        Rental saved = repository.save(rental);

        RentalCreated event = eventMapper.toRentalCreated(saved);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);

        return activateRentalUseCase.execute(
                new ActivateRentalUseCase.ActivateCommand(saved.getId(), command.operatorId()));
```

**Step 4 — Add import for `ActivateRentalUseCase`.**

```java
import com.github.jenkaby.bikerental.rental.application.usecase.ActivateRentalUseCase;
```

## 4. Validation Steps

skip