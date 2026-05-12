# Task 003: Remove Status-Patch Handling from UpdateRentalService

> **Applied Skill:** `springboot.instructions.md` — single-responsibility services;
> `java.instructions.md` — remove dead code

## 1. Objective

Remove the entire `if (patch.containsKey("status"))` block and the `startRental()` private method
from `UpdateRentalService`. Remove the now-unused `FinanceFacade` dependency from this class.
After this task, `UpdateRentalService` no longer participates in any lifecycle transition.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/UpdateRentalService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Step 1 — Remove the status-patch block.**

* **Location:** In the `execute(Long rentalId, Map<String, Object> patch)` method, locate and
  delete the following block in its entirety:

```java
        if (patch.containsKey("status")) {
            String newStatusStr = valueParser.parseString(patch.get("status"));
            RentalStatus newStatus = RentalStatus.valueOf(newStatusStr);
            log.info("Updating rental {} status to {}", rental, newStatus);

            if (RentalStatus.ACTIVE == newStatus) {
                startRental(rental);
            } else {
                rental.setStatus(newStatus);
            }
        }
```

**Step 2 — Remove the `startRental()` private method.**

* **Location:** Delete the entire `startRental(Rental rental)` private method:

```java
    private void startRental(Rental rental) {
        if (rental.getEstimatedCost().isPositive() && !financeFacade.hasHold(rental.toRentalRef())) {
            throw new HoldRequiredException(rental.getId());
        }

        // Activate rental (validations are performed in Rental.activate())
        LocalDateTime actualStartTime = LocalDateTime.now(clock);
        rental.activate(actualStartTime);

        // Publish event (inter-module)
        RentalStarted event = eventMapper.toRentalStarted(rental);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
    }
```

**Step 3 — Remove the `RentalUpdated` event guard condition.**

* **Location:** In the `execute` method, locate:

```java
        if (rental.getStatus() != RentalStatus.ACTIVE) {
            var currentState = eventMapper.toRentalState(rental);
            eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, eventMapper.toRentalUpdated(rental, previousState, currentState));
        }
```

Replace it with (always publish `RentalUpdated` since status changes no longer happen here):

```java
        var currentState = eventMapper.toRentalState(rental);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, eventMapper.toRentalUpdated(rental, previousState, currentState));
```

**Step 4 — Remove `FinanceFacade` dependency.**

* Remove `private final FinanceFacade financeFacade;` field.
* Remove `FinanceFacade financeFacade` constructor parameter.
* Remove `this.financeFacade = financeFacade;` assignment.
* Remove `import com.github.jenkaby.bikerental.finance.FinanceFacade;` import.

**Step 5 — Remove now-unused imports.**

Remove the following if they are no longer referenced in the file:

```java
import com.github.jenkaby.bikerental.rental.domain.exception.HoldRequiredException;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
import java.time.LocalDateTime;
```

> Verify each import before removing — `RentalStatus` may still be used if other patch fields
> reference it. Remove only those confirmed unused.

## 4. Validation Steps

skip
