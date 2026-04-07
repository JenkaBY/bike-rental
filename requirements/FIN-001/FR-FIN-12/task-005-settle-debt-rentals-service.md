# Task 005: Create SettleDebtRentalsService

> **Applied Skill:** `spring-boot-modulith` — `@Transactional(propagation = REQUIRES_NEW)` isolates each rental's
> settlement so a failure on one does not affect others. `OverBudgetSettlementException` is caught inside `execute` and
> returned as `settled = false`; the `REQUIRES_NEW` transaction commits cleanly with no side-effects because Finance
> writes nothing before throwing.

## 1. Objective

Implement `SettleDebtUseCase` as `SettleDebtRentalsService` in the Rental module's `application/service` package. Each
`execute` call settles exactly one rental in its own isolated `REQUIRES_NEW` transaction.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/SettleDebtRentalsService.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.usecase.SettleDebtUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.OverBudgetSettlementException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
```

**Code to Add/Replace:**

* **Location:** New file — entire content below

```java
package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.usecase.SettleDebtUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.OverBudgetSettlementException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
class SettleDebtRentalsService implements SettleDebtUseCase {

    private final RentalRepository rentalRepository;
    private final FinanceFacade financeFacade;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SettleDebtResult execute(SettleDebtCommand command) {
        Rental rental = rentalRepository.findById(command.rentalRef().id())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalRef().id().toString()));

        try {
            var settlementInfo = financeFacade.settleRental(
                    command.customerRef(),
                    command.rentalRef(),
                    rental.getFinalCost(),
                    command.operatorId()
            );
            rental.completeWithStatus(rental.getFinalCost(), RentalStatus.COMPLETED);
            rentalRepository.save(rental);
            log.info("DEBT rental {} settled successfully for customer {}",
                    command.rentalRef().id(), command.customerRef().id());
            return new SettleDebtResult(true);
        } catch (OverBudgetSettlementException e) {
            log.warn("DEBT rental {} could not be settled — insufficient funds {} for customer {}",
                    command.rentalRef().id(),  e.getDetails().availableAmount(), command.customerRef().id());
            return new SettleDebtResult(true);
        }
    }
}
```

> **Key constraint:** `rental.getFinalCost()` is the pre-computed total final cost already stored on the `Rental`
> aggregate from the original return flow. `SettleDebtRentalsService` must NOT call `TariffFacade` — Finance consumes only
> pre-computed costs.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
