# Task 007: Replace `hasPrepayment` Guard with `hasHold` in `UpdateRentalService`

> **Applied Skill:** `springboot.instructions.md` — Service layer hosts business logic; cross-module calls go through
> facades only.

## 1. Objective

Replace the deprecated `hasPrepayment(rentalId)` activation guard in `UpdateRentalService.startRental()` with the new
`hasHold(RentalRef)` call, and swap the exception it throws from `PrepaymentRequiredException` to
`HoldRequiredException`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/UpdateRentalService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Step A — Add `toRentalRef()` to the `Rental` domain class:**

* **File:** `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/model/Rental.java`

**Add import** (top of file, after the existing shared-domain import):

```java
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
```

**Add method** (anywhere in the public method body — just before or after `getEstimatedCost()` is a natural location):

```java
    public RentalRef toRentalRef() {
        return new RentalRef(id);
    }
```

---

**Step B — Remove the `PrepaymentRequiredException` import, add `HoldRequiredException` import:**

* **Location:** At the top import block.

**Remove this import line:**

```java
import com.github.jenkaby.bikerental.rental.domain.exception.PrepaymentRequiredException;
```

**Add this import line** (keep alphabetical order with the existing imports):

```java
import com.github.jenkaby.bikerental.rental.domain.exception.HoldRequiredException;
```

> Check whether this is already imported in `UpdateRentalService` and skip if so.

**Step C — Replace the guard body in `startRental`:**

* **Location:** Inside the `private void startRental(Rental rental)` method.

**Before:**

```java
    private void startRental(Rental rental) {
        if (!financeFacade.hasPrepayment(rental.getId())) {
            throw new PrepaymentRequiredException(rental.getId());
        }
```

**After:**

```java
    private void startRental(Rental rental) {
        if (rental.getEstimatedCost().isPositive() && !financeFacade.hasHold(rental.toRentalRef())) {
            throw new HoldRequiredException(rental.getId());
        }
```

> **Why the `getEstimatedCost().isPositive()` guard?** A rental whose every equipment item uses a special tariff with a
> price of `0` has a zero total planned cost. `holdFunds` is never called for such rentals (see Task 008), so no `HOLD`
> transaction exists — but activation is still valid. Requiring a hold only when `totalPlannedCost > 0` covers this case
> correctly.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
