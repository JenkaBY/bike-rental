# Task 006: Implement `holdFunds` in `FinanceFacadeImpl`

> **Applied Skill:** `java.instructions.md` — `var` for local inference; constructor injection.
> `springboot.instructions.md` — facade implementation wires use-case via constructor.
> **Depends on:** `task-003` (`FinanceFacade.holdFunds`), `task-004` (`RentalHoldUseCase`),
> `task-005` (`RecordRentalHoldService` — Spring registers it as the `RentalHoldUseCase` bean).

## 1. Objective

Wire `RecordRentalHoldService` into `FinanceFacadeImpl` by adding a `RentalHoldUseCase` dependency and
implementing the `holdFunds` method that was declared in `task-003`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacadeImpl.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports to add:**

```java
import com.github.jenkaby.bikerental.finance.application.usecase.RentalHoldUseCase;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
```

---

### Change 1 — Add `rentalHoldUseCase` field

**Location:** After the existing `private final PaymentToInfoMapper paymentToInfoMapper;` field declaration.

* **Old code:**

```java
    private final RecordPaymentUseCase recordPaymentUseCase;
private final GetPaymentsByRentalIdUseCase getPaymentsByRentalIdUseCase;
private final PaymentToInfoMapper paymentToInfoMapper;

FinanceFacadeImpl(
```

* **New code:**

```java
    private final RecordPaymentUseCase recordPaymentUseCase;
private final GetPaymentsByRentalIdUseCase getPaymentsByRentalIdUseCase;
private final PaymentToInfoMapper paymentToInfoMapper;
private final RentalHoldUseCase rentalHoldUseCase;

FinanceFacadeImpl(
```

---

### Change 2 — Add `rentalHoldUseCase` parameter to constructor

**Location:** Inside the existing constructor — add it as the last parameter and assignment.

* **Old constructor:**

```java
    FinanceFacadeImpl(
        RecordPaymentUseCase recordPaymentUseCase,
        GetPaymentsByRentalIdUseCase getPaymentsByRentalIdUseCase,
        PaymentToInfoMapper paymentToInfoMapper) {
    this.recordPaymentUseCase = recordPaymentUseCase;
    this.getPaymentsByRentalIdUseCase = getPaymentsByRentalIdUseCase;
    this.paymentToInfoMapper = paymentToInfoMapper;
}
```

* **New constructor:**

```java
    FinanceFacadeImpl(
        RecordPaymentUseCase recordPaymentUseCase,
        GetPaymentsByRentalIdUseCase getPaymentsByRentalIdUseCase,
        PaymentToInfoMapper paymentToInfoMapper,
        RentalHoldUseCase rentalHoldUseCase) {
    this.recordPaymentUseCase = recordPaymentUseCase;
    this.getPaymentsByRentalIdUseCase = getPaymentsByRentalIdUseCase;
    this.paymentToInfoMapper = paymentToInfoMapper;
    this.rentalHoldUseCase = rentalHoldUseCase;
}
```

---

### Change 3 — Implement `holdFunds` method

**Location:** Add at the very end of the class body, before the closing `}`.

* **Old (end of class):**

```java

@Override
public List<PaymentInfo> getPayments(Long rentalId) {
    return getPaymentsByRentalIdUseCase.execute(rentalId).stream()
            .map(paymentToInfoMapper::toPaymentInfo)
            .toList();
}
}
```

* **New:**

```java

@Override
public List<PaymentInfo> getPayments(Long rentalId) {
    return getPaymentsByRentalIdUseCase.execute(rentalId).stream()
            .map(paymentToInfoMapper::toPaymentInfo)
            .toList();
}

@Override
public HoldInfo holdFunds(CustomerRef customerRef, RentalRef rentalRef, Money plannedCost) {
    var command = new RentalHoldUseCase.RentalHoldCommand(customerRef, rentalRef, plannedCost);
    var result = rentalHoldUseCase.execute(command);
    return new HoldInfo(result.transactionRef(), result.recordedAt());
}
}
```

## 4. Validation Steps

```bash
./gradlew :service:test "-Dspring.profiles.active=test"
```
