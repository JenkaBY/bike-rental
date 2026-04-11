# Task 006: Implement `hasHold` in `FinanceFacadeImpl`

> **Applied Skill:** `springboot.instructions.md` — Facade implementation delegates to the domain repository; no
> business logic lives in the facade.

## 1. Objective

Implement the `hasHold(Long rentalId)` method in `FinanceFacadeImpl`, delegating to the
`TransactionRepository.existsByRentalRefAndType` method added in Task 005.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacadeImpl.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

Add the following import at the top of the file (after the existing imports):

```java
import com.github.jenkaby.bikerental.finance.domain.model.TransactionType;
import com.github.jenkaby.bikerental.finance.domain.repository.TransactionRepository;
```

> Check whether `TransactionRepository` and `TransactionType` are already imported. If so, skip those lines.

**Step A — Add `TransactionRepository` field and constructor parameter:**

* **Location:** In the `FinanceFacadeImpl` class, add `TransactionRepository` as a new `private final` field. Add it as
  the last field, directly before the constructor.

**Before** (constructor signature):

```java
    FinanceFacadeImpl(
            RecordPaymentUseCase recordPaymentUseCase,
            GetPaymentsByRentalIdUseCase getPaymentsByRentalIdUseCase,
            PaymentToInfoMapper paymentToInfoMapper,
            RentalHoldUseCase rentalHoldUseCase,
            SettleRentalUseCase settleRentalUseCase) {
        this.recordPaymentUseCase = recordPaymentUseCase;
        this.getPaymentsByRentalIdUseCase = getPaymentsByRentalIdUseCase;
        this.paymentToInfoMapper = paymentToInfoMapper;
        this.rentalHoldUseCase = rentalHoldUseCase;
        this.settleRentalUseCase = settleRentalUseCase;
    }
```

**After** (add field declaration above constructor and extend constructor):

```java
    private final TransactionRepository transactionRepository;

    FinanceFacadeImpl(
            RecordPaymentUseCase recordPaymentUseCase,
            GetPaymentsByRentalIdUseCase getPaymentsByRentalIdUseCase,
            PaymentToInfoMapper paymentToInfoMapper,
            RentalHoldUseCase rentalHoldUseCase,
            SettleRentalUseCase settleRentalUseCase,
            TransactionRepository transactionRepository) {
        this.recordPaymentUseCase = recordPaymentUseCase;
        this.getPaymentsByRentalIdUseCase = getPaymentsByRentalIdUseCase;
        this.paymentToInfoMapper = paymentToInfoMapper;
        this.rentalHoldUseCase = rentalHoldUseCase;
        this.settleRentalUseCase = settleRentalUseCase;
        this.transactionRepository = transactionRepository;
    }
```

**Step B — Add `hasHold` method implementation:**

* **Location:** Directly **after** the `holdFunds(...)` method implementation and **before** the `settleRental(...)`
  method.

**Before:**

```java
    @Override
    public SettlementInfo settleRental(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef,
```

**After:**

```java
    @Override
    public boolean hasHold(RentalRef rentalRef) {
        return transactionRepository.existsByRentalRefAndType(rentalRef, TransactionType.HOLD);
    }

    @Override
    public SettlementInfo settleRental(@NonNull CustomerRef customerRef, @NonNull RentalRef rentalRef,
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
