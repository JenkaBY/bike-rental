# Task 006: Create DebtSettlementEventListener

> **Applied Skill:** `spring-boot-modulith` — `@ApplicationModuleListener` combines `@Async` +
`@TransactionalEventListener(AFTER_COMMIT)` + its own `REQUIRES_NEW` transaction. The listener must NOT be transactional
> itself — each per-rental call to `SettleDebtUseCase` already carries `REQUIRES_NEW`. The listener's role is: query all
> DEBT rentals, loop, delegate.

## 1. Objective

Create `DebtSettlementEventListener` in the Rental module's `infrastructure/eventlistener` package. It consumes
`CustomerFundDeposited`, fetches DEBT rentals via `RentalRepository`, and calls `SettleDebtUseCase.execute(...)` per
rental.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/eventlistener/DebtSettlementEventListener.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
package com.github.jenkaby.bikerental.rental.infrastructure.eventlistener;

import com.github.jenkaby.bikerental.finance.CustomerFundDeposited;
import com.github.jenkaby.bikerental.rental.application.usecase.SettleDebtUseCase;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
```

**Code to Add/Replace:**

* **Location:** New file — entire content below

```java
package com.github.jenkaby.bikerental.rental.infrastructure.eventlistener;

import com.github.jenkaby.bikerental.finance.CustomerFundDeposited;
import com.github.jenkaby.bikerental.rental.application.usecase.SettleDebtUseCase;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DebtSettlementEventListener {

    private final RentalRepository rentalRepository;
    private final SettleDebtUseCase settleDebtUseCase;

    @ApplicationModuleListener
    public void onCustomerFundDeposited(CustomerFundDeposited event) {
        log.info("Received CustomerFundDeposited for customerId={}, transactionId={}",
                event.customerId(), event.transactionId());

        var customerRef = new CustomerRef(event.customerId());
        var debtRentals = rentalRepository.getCustomerDebtRentals(customerRef);

        if (debtRentals.isEmpty()) {
            log.debug("No DEBT rentals found for customerId={}", event.customerId());
            return;
        }

        log.info("Attempting to settle {} DEBT rental(s) for customerId={}", debtRentals.size(), event.customerId());

        for (var rental : debtRentals) {
            var command = new SettleDebtUseCase.SettleDebtCommand(
                    customerRef,
                    RentalRef.of(rental.getId()),
                    event.operatorId()
            );
            var result = settleDebtUseCase.execute(command);
            if (!result.settled()) {
                // no sense to settle other rentals if one fails
                return;
            }
        }
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
