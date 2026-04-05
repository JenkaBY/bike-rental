# Task 008: Refactor `ReturnEquipmentService` — Replace Deprecated Finance Calls with `settleRental`

> **Applied Skill:** `java.instructions.md` — expressive naming; `@Nullable`; remove dead code.
> `springboot.instructions.md` — `@Transactional` boundary; slim services.
> **Depends on:** task-004 (`SettlementInfo`), task-007 (`FinanceFacadeImpl.settleRental`).

## 1. Objective

Replace the deprecated `financeFacade.getPayments()` / `financeFacade.recordAdditionalPayment()` calls in
`ReturnEquipmentService` with a single `financeFacade.settleRental()` call that is invoked **only on the
final return** (when all equipment is returned). Partial returns no longer trigger any finance call — the
hold stays intact until the last item is returned.

Additionally:

- Update `ReturnEquipmentResult` to replace `PaymentInfo paymentInfo` and `Money additionalPayment` with a
  single `@Nullable SettlementInfo settlementInfo` field.
- Update `RentalCommandMapper.toReturnResponse()` and `RentalReturnResponse` to remove the now-dead
  `paymentInfo` and `additionalPayment` fields from the HTTP response.

**New settlement logic (final return path):**

- `totalFinalCost = previouslyReturnedCost + totalCost` (sum of ALL equipment costs across all partial
  returns plus the current batch).
-
`settlementInfo = financeFacade.settleRental(CustomerRef.of(rental.getCustomerId()), RentalRef.of(rental.getId()), totalFinalCost, command.operatorId())`.
- `rental.complete(totalFinalCost)` — sets `finalCost` on the rental and transitions status to `COMPLETED`.
- `OverBudgetSettlementException` propagates uncaught from `settleRental`; FR-FIN-08 will add the catch
  branch in a later task.

**Partial return path:**

- Calculate costs for the items being returned, mark them `RETURNED`, save and return without finance call.
  (`rental.calculateActualDuration()` still runs to persist `actualReturnAt` and `actualDuration` on the
  rental for later reference.)

## 2. File to Modify / Create

### File 1 — `ReturnEquipmentResult.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/usecase/ReturnEquipmentResult.java`
* **Action:** Modify Existing File

### File 2 — `ReturnEquipmentService.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`
* **Action:** Modify Existing File

### File 3 — `RentalReturnResponse.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/dto/RentalReturnResponse.java`
* **Action:** Modify Existing File

### File 4 — `RentalCommandMapper.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/mapper/RentalCommandMapper.java`
* **Action:** Modify Existing File

## 3. Code Implementation

### File 1 — `ReturnEquipmentResult.java`

Replace the entire file body. Remove `Money additionalPayment` and `PaymentInfo paymentInfo`; add
`@Nullable SettlementInfo settlementInfo`.

* **Old code:**

```java
package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.RentalCost;

import java.util.Map;

public record ReturnEquipmentResult(
        Rental rental,
        Map<Long, RentalCost> breakDownCosts,
        Money additionalPayment,
        PaymentInfo paymentInfo
) {
}
```

* **New code:**

```java
package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.finance.SettlementInfo;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public record ReturnEquipmentResult(
        Rental rental,
        Map<Long, RentalCost> breakDownCosts,
        @Nullable SettlementInfo settlementInfo
) {
}
```

---

### File 2 — `ReturnEquipmentService.java`

Replace the entire file. The key structural changes are:

1. Remove imports: `PaymentInfo`, `PaymentMethod` (no longer used).
2. Add imports: `SettlementInfo`, `CustomerRef`, `RentalRef`.
3. Replace the deprecated finance call section with a conditional `settleRental` block.

* **Old code (full file):**

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentResult;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipmentStatus;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import com.github.jenkaby.bikerental.tariff.TariffFacade;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
class ReturnEquipmentService implements ReturnEquipmentUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final RentalDurationCalculator durationCalculator;
    private final TariffFacade tariffFacade;
    private final FinanceFacade financeFacade;
    private final RentalEventMapper eventMapper;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    ReturnEquipmentService(
            RentalRepository rentalRepository,
            RentalDurationCalculator durationCalculator,
            TariffFacade tariffFacade,
            FinanceFacade financeFacade,
            RentalEventMapper eventMapper,
            EventPublisher eventPublisher,
            Clock clock) {
        this.rentalRepository = rentalRepository;
        this.durationCalculator = durationCalculator;
        this.tariffFacade = tariffFacade;
        this.financeFacade = financeFacade;
        this.eventMapper = eventMapper;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    @NonNull
    public ReturnEquipmentResult execute(@NonNull ReturnEquipmentCommand command) {
        log.info("Processing equipment return for rentalId={}, equipmentIds={}, equipmentUids={}",
                command.rentalId(), command.equipmentIds(), command.equipmentUids());

        LocalDateTime returnTime = LocalDateTime.now(clock);
        Rental rental = findRental(command);
        if (!rental.hasActiveStatus()) {
            throw new InvalidRentalStatusException(rental.getStatus(), RentalStatus.ACTIVE);
        }
        var durationResult = rental.calculateActualDuration(durationCalculator, returnTime);
        var equipmentsToReturn = rental.equipmentsToReturn(command.getEquipmentIds(), command.getEquipmentUids(), returnTime);
        Money totalCost = Money.zero();
        Map<Long, RentalCost> rentalCostMapToEquipment = new HashMap<>();
        for (var equipment : equipmentsToReturn) {
            RentalCost cost = tariffFacade.calculateRentalCost(
                    equipment.getTariffId(),
                    rental.getActualDuration(),
                    durationResult.billableMinutes(),
                    rental.getPlannedDuration()
            );
            equipment.setFinalCost(cost.totalCost());
            rentalCostMapToEquipment.put(equipment.getEquipmentId(), cost);
            totalCost = totalCost.add(cost.totalCost());
        }
// 1. Sum final costs of equipment returned in PREVIOUS partial returns
        Money previouslyReturnedCost = rental.getEquipments().stream()
                .filter(e -> e.getStatus() == RentalEquipmentStatus.RETURNED)
                .filter(e -> !equipmentsToReturn.contains(e))
                .map(RentalEquipment::getFinalCost)
                .reduce(Money.zero(), Money::add);

// 2. Estimated cost of equipment STILL ACTIVE after this return
        Money remainingEstimatedCost = rental.getEquipments().stream()
                .filter(e -> e.getStatus() != RentalEquipmentStatus.RETURNED)
                .map(RentalEquipment::getEstimatedCost)
                .reduce(Money.zero(), Money::add);


        List<PaymentInfo> paymentsMade = financeFacade.getPayments(rental.getId());
        Money paymentsTotalAmount = paymentsMade.stream()
                .map(PaymentInfo::amount)
                .reduce(Money.zero(), Money::add);

// 3. Correct balance:
// toPay = previouslyReturned + currentReturned + remainingEstimated - allPayments
        Money toPay = previouslyReturnedCost
                .add(totalCost)
                .add(remainingEstimatedCost)
                .subtract(paymentsTotalAmount);
//        Money toPay = totalCost.subtract(paymentsTotalAmount);

        PaymentInfo paymentInfo = null;
        if (toPay.isPositive()) {
            if (command.paymentMethod() == null) {
                throw new IllegalArgumentException("Payment method is required when additional payment is needed");
            }
            paymentInfo = financeFacade.recordAdditionalPayment(
                    rental.getId(),
                    toPay,
                    command.paymentMethod(),
                    command.operatorId()
            );
            log.info("Recorded additional payment {} for rental {}", toPay, rental.getId());
        }

        rental.complete(paymentsTotalAmount.add(Optional.ofNullable(paymentInfo).map(PaymentInfo::amount).orElse(Money.zero())));

        Rental saved = rentalRepository.save(rental);

        RentalCompleted event = eventMapper.toRentalCompleted(saved, returnTime, totalCost);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
        log.info("Published RentalCompleted event for rental {}", saved.getId());

        return new ReturnEquipmentResult(saved, rentalCostMapToEquipment, toPay, paymentInfo);
    }

    private Rental findRental(ReturnEquipmentCommand command) {
        return rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));
    }
}
```

* **New code (full file):**

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.finance.SettlementInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentResult;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipmentStatus;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import com.github.jenkaby.bikerental.tariff.TariffFacade;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
class ReturnEquipmentService implements ReturnEquipmentUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final RentalDurationCalculator durationCalculator;
    private final TariffFacade tariffFacade;
    private final FinanceFacade financeFacade;
    private final RentalEventMapper eventMapper;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    ReturnEquipmentService(
            RentalRepository rentalRepository,
            RentalDurationCalculator durationCalculator,
            TariffFacade tariffFacade,
            FinanceFacade financeFacade,
            RentalEventMapper eventMapper,
            EventPublisher eventPublisher,
            Clock clock) {
        this.rentalRepository = rentalRepository;
        this.durationCalculator = durationCalculator;
        this.tariffFacade = tariffFacade;
        this.financeFacade = financeFacade;
        this.eventMapper = eventMapper;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    @NonNull
    public ReturnEquipmentResult execute(@NonNull ReturnEquipmentCommand command) {
        log.info("Processing equipment return for rentalId={}, equipmentIds={}, equipmentUids={}",
                command.rentalId(), command.equipmentIds(), command.equipmentUids());

        LocalDateTime returnTime = LocalDateTime.now(clock);
        Rental rental = findRental(command);
        if (!rental.hasActiveStatus()) {
            throw new InvalidRentalStatusException(rental.getStatus(), RentalStatus.ACTIVE);
        }

        var durationResult = rental.calculateActualDuration(durationCalculator, returnTime);
        var equipmentsToReturn = rental.equipmentsToReturn(
                command.getEquipmentIds(), command.getEquipmentUids(), returnTime);

        Money totalCost = Money.zero();
        Map<Long, RentalCost> rentalCostMapToEquipment = new HashMap<>();
        for (var equipment : equipmentsToReturn) {
            RentalCost cost = tariffFacade.calculateRentalCost(
                    equipment.getTariffId(),
                    rental.getActualDuration(),
                    durationResult.billableMinutes(),
                    rental.getPlannedDuration()
            );
            equipment.setFinalCost(cost.totalCost());
            rentalCostMapToEquipment.put(equipment.getEquipmentId(), cost);
            totalCost = totalCost.add(cost.totalCost());
        }

        if (!rental.allEquipmentReturned()) {
            Rental saved = rentalRepository.save(rental);
            log.info("Partial return recorded for rental {}", saved.getId());
            return new ReturnEquipmentResult(saved, rentalCostMapToEquipment, null);
        }

        Money previouslyReturnedCost = rental.getEquipments().stream()
                .filter(e -> e.getStatus() == RentalEquipmentStatus.RETURNED)
                .filter(e -> !equipmentsToReturn.contains(e))
                .map(RentalEquipment::getFinalCost)
                .reduce(Money.zero(), Money::add);

        var totalFinalCost = previouslyReturnedCost.add(totalCost);
        @Nullable SettlementInfo settlementInfo = financeFacade.settleRental(
                CustomerRef.of(rental.getCustomerId()),
                RentalRef.of(rental.getId()),
                totalFinalCost,
                command.operatorId()
        );

        rental.complete(totalFinalCost);
        Rental saved = rentalRepository.save(rental);

        RentalCompleted event = eventMapper.toRentalCompleted(saved, returnTime, totalFinalCost);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
        log.info("Published RentalCompleted event for rental {}", saved.getId());

        return new ReturnEquipmentResult(saved, rentalCostMapToEquipment, settlementInfo);
    }

    private Rental findRental(ReturnEquipmentCommand command) {
        return rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        Rental.class, command.rentalId().toString()));
    }
}
```

---

### File 3 — `RentalReturnResponse.java`

Remove the `additionalPayment` and `paymentInfo` fields from the response record. They belong to the
deprecated payment flow and are replaced by the settlement being internal to the Finance module.

* **Old code:**

```java
@Schema(description = "Result of equipment return operation")
public record RentalReturnResponse(
        @Schema(description = "Updated rental details") RentalResponse rental,
        @Schema(description = "Cost breakdown for the rental") List<CostBreakdown> costs,
        @Schema(description = "Additional payment charged at return (0 if prepayment covered all)", example = "50.00") BigDecimal additionalPayment,
        @Schema(description = "Payment info for the additional charge") PaymentInfoResponse paymentInfo
) {
```

* **New code:**

```java
@Schema(description = "Result of equipment return operation")
public record RentalReturnResponse(
        @Schema(description = "Updated rental details") RentalResponse rental,
        @Schema(description = "Cost breakdown for the rental") List<CostBreakdown> costs
) {
```

Also remove the now-unused `java.math.BigDecimal` import from the same file if it is only used for those
two removed fields.

* **Old import to remove:**

```java
import java.math.BigDecimal;
```

---

### File 4 — `RentalCommandMapper.java`

Update `toReturnResponse` to no longer reference `additionalPayment` or `paymentInfo`.

* **Old code:**

```java
    public RentalReturnResponse toReturnResponse(ReturnEquipmentResult result) {
        RentalResponse rentalResponse = rentalQueryMapper.toResponse(result.rental());
        var costsBreakdown = result.breakDownCosts().entrySet().stream()
                .map(entry -> toCostBreakdown(entry.getKey(), entry.getValue()))
                .toList();
        BigDecimal additionalPayment = result.additionalPayment() != null ? result.additionalPayment().amount() : null;
        return new RentalReturnResponse(rentalResponse, costsBreakdown, additionalPayment, paymentInfoMapper.toResponse(result.paymentInfo()));
    }
```

* **New code:**

```java
    public RentalReturnResponse toReturnResponse(ReturnEquipmentResult result) {
        RentalResponse rentalResponse = rentalQueryMapper.toResponse(result.rental());
        var costsBreakdown = result.breakDownCosts().entrySet().stream()
                .map(entry -> toCostBreakdown(entry.getKey(), entry.getValue()))
                .toList();
        return new RentalReturnResponse(rentalResponse, costsBreakdown);
    }
```

Also remove the `BigDecimal` import from `RentalCommandMapper.java` if it is only used inside the old
`toReturnResponse` method.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
./gradlew :service:test "-Dspring.profiles.active=test" --tests BikeRentalApplicationTest
./gradlew :service:test "-Dspring.profiles.active=test" --tests RentalCommandControllerTest
```
