# Task 003: Delete Finance Application Layer and Clean FinanceFacade

> **Applied Skill:** No dedicated skill file — follows hexagonal architecture conventions in `AGENTS.md` (facade =
> published cross-module contract, application use-case interfaces and service implementations).

## 1. Objective

Delete `RecordPaymentUseCase` and `RecordPaymentService` from the Finance application layer, then remove the five
`@Deprecated(forRemoval = true)` methods from the `FinanceFacade` interface and their implementations in
`FinanceFacadeImpl`, together with all now-unused fields, constructor parameters, and imports. After this task the
Finance facade exposes only the hold-based contract introduced by FR-FIN-14: `holdFunds`, `hasHold`, `settleRental`.

---

## 2. Files to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/usecase/RecordPaymentUseCase.java`
* **Action:** Delete file entirely.

---

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordPaymentService.java`
* **Action:** Delete file entirely.

---

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacade.java`
* **Action:** Modify existing file.

---

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacadeImpl.java`
* **Action:** Modify existing file.

---

## 3. Code Implementation

### 3.1 Delete `RecordPaymentUseCase.java` and `RecordPaymentService.java`

Delete both files listed above. No replacements are needed.

---

### 3.2 Modify `FinanceFacade.java` — remove deprecated methods and unused imports

**Replace the entire file content** with the version below. The `PaymentInfo`, `PaymentMethod`, `List`, and `Optional`
imports are removed because they were only used by the deleted methods.

```java
package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.NonNull;

public interface FinanceFacade {

    HoldInfo holdFunds(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef,
                       @NonNull Money plannedCost, @NonNull String operatorId);

    boolean hasHold(RentalRef rentalRef);

    SettlementInfo settleRental(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef,
                                @NonNull Money finalCost, @NonNull String operatorId);
}
```

---

### 3.3 Modify `FinanceFacadeImpl.java` — remove deprecated implementations and dead fields

**Replace the entire file content** with the version below. Removed:

- `IS_PREPAYMENT` Predicate constant
- Three fields: `recordPaymentUseCase`, `getPaymentsByRentalIdUseCase`, `paymentToInfoMapper`
- Corresponding constructor parameters and assignments
- Five method overrides: `recordPrepayment`, `recordAdditionalPayment`, `hasPrepayment`, `getPrepayment`, `getPayments`
- Eight imports: `PaymentToInfoMapper`, `GetPaymentsByRentalIdUseCase`, `RecordPaymentUseCase`, `Payment`,
  `PaymentType`, `java.util.List`, `java.util.Optional`, `java.util.function.Predicate`

```java
package com.github.jenkaby.bikerental.finance;

import com.github.jenkaby.bikerental.finance.application.usecase.RentalHoldUseCase;
import com.github.jenkaby.bikerental.finance.application.usecase.SettleRentalUseCase;
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
class FinanceFacadeImpl implements FinanceFacade {

    private final RentalHoldUseCase rentalHoldUseCase;
    private final SettleRentalUseCase settleRentalUseCase;
    private final TransactionRepository transactionRepository;

    FinanceFacadeImpl(
            RentalHoldUseCase rentalHoldUseCase,
            SettleRentalUseCase settleRentalUseCase,
            TransactionRepository transactionRepository) {
        this.rentalHoldUseCase = rentalHoldUseCase;
        this.settleRentalUseCase = settleRentalUseCase;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public HoldInfo holdFunds(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef, @NonNull Money plannedCost, @NonNull String operatorId) {
        var command = new RentalHoldUseCase.RentalHoldCommand(customerRef, rentalRef, plannedCost, operatorId);
        var result = rentalHoldUseCase.execute(command);
        return new HoldInfo(result.transactionRef(), result.recordedAt());
    }

    @Override
    public SettlementInfo settleRental(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef,
                                       @NonNull Money finalCost, @NonNull String operatorId) {
        var command = new SettleRentalUseCase.SettleRentalCommand(customerRef, rentalRef, finalCost, operatorId);
        var result = settleRentalUseCase.execute(command);
        return new SettlementInfo(result.captureTransactionRefs(), result.releaseTransactionRef(), result.recordedAt());
    }

    @Override
    public boolean hasHold(RentalRef rentalRef) {
        return transactionRepository.existsByRentalRefAndType(rentalRef, TransactionType.HOLD);
    }
}
```

---

## 4. Validation Steps

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

Expected result: `BUILD SUCCESSFUL` with zero compilation errors in the `service` module.
