# Task 002: Publish CustomerFundDeposited from RecordDepositService

> **Applied Skill:** `spring-boot-modulith` — Events must be published via the shared `EventPublisher` port inside an
> active transaction (`Propagation.MANDATORY` is enforced by `SpringApplicationEventPublisher`). Spring Modulith records
> the event to `event_publication` and delivers it after the transaction commits.

## 1. Objective

Extend `RecordDepositService.execute()` to publish a `CustomerFundDeposited` event after the deposit transaction is
persisted, matching the pattern used in `RecordPaymentService`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordDepositService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.CustomerFundDeposited;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
```

**Code to Add/Replace:**

* **Location 1:** Add `EventPublisher` to the constructor field list. The class uses `@RequiredArgsConstructor` so only
  a field declaration is needed. Add it directly below the `Clock clock;` field.

```java
    private final Clock clock;
    private final EventPublisher eventPublisher;
```

* **Location 2:** Add a constant for the exchange name directly above the class declaration line
  `public class RecordDepositService`:

```java
    private static final String FINANCE_EVENTS_EXCHANGER = "finance-events";
```

* **Location 3:** Append event publication at the very end of the `execute()` method, just before the final `return`
  statement:

Replace:

```java
        transactionRepository.save(transaction);

        return new DepositResult(transactionId, now);
    }
```

With:

```java
        transactionRepository.save(transaction);

        eventPublisher.publish(FINANCE_EVENTS_EXCHANGER, new CustomerFundDeposited(
                command.customerId(),
                transactionId,
                command.operatorId(),
                now
        ));

        return new DepositResult(transactionId, now);
    }
```

> **Note:** The idempotency early-return path at the top of `execute()` must NOT publish the event — it covers a
> duplicate request, so the event was already published on the original call. The existing early-return block is
> unchanged.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
