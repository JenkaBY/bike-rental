# Task 008: Integrate `holdFunds` into `CreateRentalService`

> **Applied Skill:** `springboot.instructions.md` — Business logic lives in `@Service`; the `@Transactional` boundary
> already wraps the method; the hold call is placed after `repository.save` (so the rental `id` is assigned) and before
> event publishing.

## 1. Objective

Call `FinanceFacade.holdFunds(...)` inside the existing `@Transactional` method
`CreateRentalService.execute(CreateRentalCommand)`, after `repository.save(rental)` and before
`eventPublisher.publish(...)`. Also add a zero-cost guard before the hold call.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CreateRentalService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Step A — Add required imports:**

Add the following imports at the top of the file, after the existing imports:

```java
import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
```

> Check if any of these are already imported before adding them.

**Step B — Add `FinanceFacade` as a field and constructor parameter:**

* **Location:** In the field list, add `FinanceFacade` as the last field before the constructor.

**Before** (constructor):

```java
    CreateRentalService(
            RentalRepository repository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            TariffFacade tariffFacade,
            EventPublisher eventPublisher,
            RentalEventMapper eventMapper, Clock clock, RequestedEquipmentValidator validator) {
        this.repository = repository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.tariffFacade = tariffFacade;
        this.eventPublisher = eventPublisher;
        this.eventMapper = eventMapper;
        this.clock = clock;
        this.validator = validator;
    }
```

**After** (add field + constructor parameter):

```java
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

**Step C — Insert hold logic after `repository.save` and before event publishing:**

* **Location:** In the `execute(CreateRentalCommand)` method — the section around `repository.save(rental)`.

**Before:**

```java
        Rental saved = repository.save(rental);

        RentalCreated event = eventMapper.toRentalCreated(saved);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
        return saved;
```

**After:**

```java
        Rental saved = repository.save(rental);

        if (saved.getEstimatedCost().isPositive()) {
            financeFacade.holdFunds(
                    new CustomerRef(saved.getCustomerId()),
                    saved.toRentalRef(),
                    saved.getEstimatedCost());
        }

        RentalCreated event = eventMapper.toRentalCreated(saved);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
        return saved;
```

> **Why skip `holdFunds` when `getEstimatedCost()` is zero?** A rental where every equipment item uses a special tariff
> with a price of `0` produces a zero total planned cost. Calling `holdFunds` with `Money.zero()` would be rejected by the
> Finance module's own guard. Instead, `holdFunds` is skipped entirely — the rental is free and no `HOLD` transaction is
> required. `UpdateRentalService` mirrors this logic (Task 007) and allows activation without a hold when the estimated
> cost is zero.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
