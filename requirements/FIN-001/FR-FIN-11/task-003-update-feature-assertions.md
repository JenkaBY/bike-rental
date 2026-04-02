# Task 003: Update Component Test Feature File Assertions

> **Applied Skill:** `spring-boot-java-cucumber` (component test conventions) — update Gherkin data-table balance
> assertions to match corrected asset sub-ledger arithmetic.
> **Depends on:** `task-002` (SubLedger arithmetic fix must be in place for tests to pass).

## 1. Objective

Update balance assertions for system sub-ledger rows in `deposits.feature` and `withdrawals.feature` to match the
corrected T-account arithmetic for asset sub-ledgers:

- **Deposit:** DEBIT on an asset sub-ledger now *increases* balance → `0 + 50 = 50` (was `-50`)
- **Withdrawal:** CREDIT on an asset sub-ledger now *decreases* balance → `0 - 30 = -30` (was `30`)

No step definitions, transformers, or other Gherkin text changes — only the balance value in the data tables.

## 2. File to Modify / Create

### File 1

* **File Path:** `component-test/src/test/resources/features/finance/deposits.feature`
* **Action:** Modify Existing File

### File 2

* **File Path:** `component-test/src/test/resources/features/finance/withdrawals.feature`
* **Action:** Modify Existing File

---

## 3. Code Implementation

### File 1 — `deposits.feature`

**Location:** Inside `Scenario Outline: Successful cash deposit increases customer wallet balance`, in the
`the following sub-ledger records were persisted in db` data table. Change the `balance` value for the
`<subLedger>` row from `-50` to `50`.

* **Old assertion row:**

```gherkin
      | <subLedger> | ACC_S     | <ledgerType>    | 1       | -50     |
```

* **New assertion row:**

```gherkin
      | <subLedger> | ACC_S     | <ledgerType>    | 1       | 50      |
```

Full context around the change (for precise location):

```gherkin
    And the following sub-ledger records were persisted in db
      | id          | accountId | ledgerType      | version | balance |
      | L_C_W2      | ACC2      | CUSTOMER_WALLET | 2       | 50      |
      | L_C_H2      | ACC2      | CUSTOMER_HOLD   | 2       | 0.00    |
      | <subLedger> | ACC_S     | <ledgerType>    | 1       | 50      |
```

---

### File 2 — `withdrawals.feature`

**Location:** Inside `Scenario Outline: Successful partial cash withdrawal reduces customer wallet and credits the
payout sub-ledger`, in the `the following sub-ledger records were persisted in db` data table. Change the
`balance` value for the `<subLedger>` row from `30` to `-30`.

* **Old assertion row:**

```gherkin
      | <subLedger> | ACC_S     | <ledgerType>    | 1       | 30      |
```

* **New assertion row:**

```gherkin
      | <subLedger> | ACC_S     | <ledgerType>    | 1       | -30     |
```

Full context around the change (for precise location):

```gherkin
    And the following sub-ledger records were persisted in db
      | id          | accountId | ledgerType      | version | balance          |
      | L_C_W2      | ACC2      | CUSTOMER_WALLET | 2       | 50               |
      | L_C_H2      | ACC2      | CUSTOMER_HOLD   | 2       | 20               |
      | <subLedger> | ACC_S     | <ledgerType>    | 1       | -30              |
```

---

## 4. Validation Steps

```bash
./gradlew :component-test:test "-Dspring.profiles.active=test"
```
