# Task 011: Update RecordDepositService and ApplyAdjustmentService with Typed Account Accessors

> **Applied Skill:** `java.instructions.md` — replace generic `getSubLedger(LedgerType)` call sites with the
> typed convenience methods introduced on `CustomerAccount` and `SystemAccount` in task-008.

## 1. Objective

Update the two existing application services that call `getSubLedger(LedgerType)` directly so they use the
expressive typed accessors instead:

| Old call                                                   | New call                        |
|------------------------------------------------------------|---------------------------------|
| `customerAccount.getSubLedger(LedgerType.CUSTOMER_WALLET)` | `customerAccount.getWallet()`   |
| `customerAccount.getCustomerWallet()`                      | `customerAccount.getWallet()`   |
| `systemAccount.getSubLedger(LedgerType.ADJUSTMENT)`        | `systemAccount.getAdjustment()` |

The `@NonNull Account getCustomerAccount(...)` private method in `RecordDepositService` is also updated to
return `CustomerAccount` to match the narrowed port return type.

## 2. File to Modify / Create

* **File Path (1):**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordDepositService.java`
* **Action:** Modify Existing File

* **File Path (2):**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/ApplyAdjustmentService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

### File 1 — `RecordDepositService.java`

#### Change 1 — Add `CustomerAccount` import

* **Location:** Add after `import com.github.jenkaby.bikerental.finance.domain.model.Account;`

```java
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
```

#### Change 2 — Replace `getCustomerWallet()` with `getWallet()`

* **Location:** Inside `execute()`, replace the `creditSubLedger` assignment.

Before:

```java
        var debitSubLedger = systemAccount.getSubLedger(debitLedgerType);
        var creditSubLedger = customerAccount.getCustomerWallet();

        var debitChange = debitSubLedger.debit(command.amount());
        var creditChange = creditSubLedger.credit(command.amount());
```

After:

```java
        var debitSubLedger = systemAccount.getSubLedger(debitLedgerType);
        var creditSubLedger = customerAccount.getWallet();

        var debitChange = debitSubLedger.debit(command.amount());
        var creditChange = creditSubLedger.credit(command.amount());
```

---

### File 2 — `ApplyAdjustmentService.java`

#### Change 1 — Replace `getSubLedger(LedgerType.CUSTOMER_WALLET)` and `getSubLedger(LedgerType.ADJUSTMENT)`

* **Location:** Inside `execute()`, replace the two sub-ledger resolution lines.

Before:

```java
        var customerWallet = customerAccount.getSubLedger(LedgerType.CUSTOMER_WALLET);
        var adjustmentSubLedger = systemAccount.getSubLedger(LedgerType.ADJUSTMENT);
```

After:

```java
        var customerWallet = customerAccount.getWallet();
        var adjustmentSubLedger = systemAccount.getAdjustment();
```

> Note: `LedgerType` is no longer referenced anywhere in `ApplyAdjustmentService` after this change. The
> import `import com.github.jenkaby.bikerental.finance.domain.model.*;` is a wildcard, so no import line
> needs removing — the compiler will silently ignore the unused type from the wildcard import.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
