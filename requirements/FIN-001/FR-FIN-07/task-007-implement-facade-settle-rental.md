# Task 007: Implement `FinanceFacadeImpl.settleRental`

> **Applied Skill:** `springboot.instructions.md` — constructor injection; facade delegates to use-case.
> `java.instructions.md` — no inline comments; expressive naming.
> **Depends on:** task-004 (`SettlementInfo` + `FinanceFacade.settleRental`), task-005
> (`SettleRentalUseCase`), task-006 (`SettleRentalService` must be present as a Spring bean).

## 1. Objective

Wire `SettleRentalUseCase` into `FinanceFacadeImpl` and implement the `settleRental` facade method. The
method maps the facade parameters to a `SettleRentalCommand`, calls the use case, and maps the
`SettlementResult` back to `SettlementInfo`. `OverBudgetSettlementException` and
`InsufficientHoldException` propagate uncaught — the Rental module handles them.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacadeImpl.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required (add to the existing import block):**

```java
import com.github.jenkaby.bikerental.finance.application.usecase.SettleRentalUseCase;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
```

### Step 1 — Add `SettleRentalUseCase` field

**Location:** Inside the class body, after the existing `private final RentalHoldUseCase rentalHoldUseCase`
field.

* **Old code:**

```java
    private final RentalHoldUseCase rentalHoldUseCase;
```

* **New code:**

```java
    private final RentalHoldUseCase rentalHoldUseCase;
    private final SettleRentalUseCase settleRentalUseCase;
```

> **Note:** `@Service` + `@RequiredArgsConstructor` (or the explicit constructor that Lombok generates)
> auto-wires all `final` fields. If `FinanceFacadeImpl` uses an explicit constructor (not Lombok), add
> `SettleRentalUseCase settleRentalUseCase` as the last parameter and assign it to `this.settleRentalUseCase`
> in the body.

---

### Step 2 — Implement `settleRental`

**Location:** Add the method after the existing `holdFunds` implementation — at the end of the class, before
the closing brace.

* **Code to add:**

```java
    @Override
    public SettlementInfo settleRental(CustomerRef customerRef, RentalRef rentalRef, Money finalCost, String operatorId) {
        var command = new SettleRentalUseCase.SettleRentalCommand(customerRef, rentalRef, finalCost, operatorId);
        var result = settleRentalUseCase.execute(command);
        return new SettlementInfo(result.captureTransactionRefs(), result.releaseTransactionRef(), result.recordedAt());
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
