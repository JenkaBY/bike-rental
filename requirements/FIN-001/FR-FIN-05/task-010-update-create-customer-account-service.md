# Task 010: Update CreateCustomerAccountService for CustomerAccount

> **Applied Skill:** `java.instructions.md` — use typed subclass builder; no `accountType` field to pass
> since `CustomerAccount.getAccountType()` is hardcoded in the class itself.

## 1. Objective

Switch `CreateCustomerAccountService` from `Account.builder()` to `CustomerAccount.builder()`. The
`accountType` field is removed from the builder call because `CustomerAccount.getAccountType()` now
returns `AccountType.CUSTOMER` implicitly.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/CreateCustomerAccountService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

* **Location:** Add the following import after the existing `Account` import line.

```java
import com.github.jenkaby.bikerental.finance.domain.model.CustomerAccount;
```

* Also remove the now-unused import:

```java
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
```

**Code to Add/Replace:**

* **Location:** Inside `CreateCustomerAccountService.execute()`, replace the `Account.builder()` block.

Before:

```java
        var account = Account.builder()
                .id(uuidGenerator.generate())
                .accountType(AccountType.CUSTOMER)
                .customerRef(CustomerRef.of(customerId))
                .subLedgers(List.of(wallet, hold))
                .build();
```

After:

```java
        var account = CustomerAccount.builder()
                .id(uuidGenerator.generate())
                .customerRef(CustomerRef.of(customerId))
                .subLedgers(List.of(wallet, hold))
                .build();
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
