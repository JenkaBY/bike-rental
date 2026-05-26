# Task 002: Implement calendar-day billing logic in FlatFeeTariffV2

> **Applied Skill:** `java.instructions.md` — pattern matching, streams; `springboot.instructions.md` — handle edge
> cases

## 1. Objective

Replace `FlatFeeTariffV2.calculateCost(Duration)` with the new `calculateCost(LocalDateTime startAt, LocalDateTime
returnAt)` signature. The new implementation counts the number of **distinct calendar dates** spanned by the
`[startAt.toLocalDate(), returnAt.toLocalDate()]` closed interval using `ChronoUnit.DAYS.between`. Minimum 1 day is
preserved for zero or negative durations.

> ⚠️ This task still breaks compilation until task-003 is complete (other subclasses not yet migrated).

## 2. File to Modify

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/model/FlatFeeTariffV2.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required — replace the existing import block with:**

```java
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import com.github.jenkaby.bikerental.tariff.RentalCostV2;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseRentalCostV2;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
```

> `java.time.Duration` is still needed for the guard check. `ChronoUnit` replaces the removed
> `getNumberOfDays(Duration)` helper.

---

**Replace the entire `calculateCost` method.**

* **Location:** The `@Override calculateCost` method — replaces the complete method body.
* **Remove:**

```java
    @Override
    public RentalCostV2 calculateCost(Duration duration) {
        int days = isNegative(duration) ? 1 : getNumberOfDays(duration);
        Money cost = issuanceFee.multiply(BigDecimal.valueOf(days));
        String message = String.format("Flat fee: %s*%dd = %s", issuanceFee, days, cost);
        return new BaseRentalCostV2(cost, new BreakdownCostDetails.FlatFee(message,
                new BreakdownCostDetails.FlatFee.Details(issuanceFee.toString(), days, cost.toString())));
    }
```

* **Replace with:**

```java
    @Override
    public RentalCostV2 calculateCost(LocalDateTime startAt, LocalDateTime returnAt) {
        Duration duration = Duration.between(startAt, returnAt);
        int days = isNegative(duration) ? 1 : countCalendarDays(startAt.toLocalDate(), returnAt.toLocalDate());
        Money cost = issuanceFee.multiply(BigDecimal.valueOf(days));
        String message = String.format("Flat fee: %s*%dd = %s", issuanceFee, days, cost);
        return new BaseRentalCostV2(cost, new BreakdownCostDetails.FlatFee(message,
                new BreakdownCostDetails.FlatFee.Details(issuanceFee.toString(), days, cost.toString())));
    }

    private static int countCalendarDays(LocalDate startDate, LocalDate endDate) {
        return (int) (ChronoUnit.DAYS.between(startDate, endDate) + 1);
    }
```

> **Why `DAYS.between(start, end) + 1`?**
> `ChronoUnit.DAYS.between` returns the number of full days between two dates (exclusive of the end). Adding `1`
> makes the interval inclusive on both ends. Example: `DAYS.between(June 1, June 2) = 1 → + 1 = 2 days` (June 1
> and June 2 both held). Example: `DAYS.between(June 1, June 1) = 0 → + 1 = 1 day`.

## 4. Validation Steps

skip