# Task 005: Write unit tests for FlatFeeTariffV2 calendar-day billing

> **Applied Skill:** `java.instructions.md` — use AssertJ, pattern matching; `springboot.instructions.md` — N/A
> (pure domain unit test, no Spring context)

## 1. Objective

Create a new JUnit 5 unit test class that verifies all four BDD scenarios from FR-1 directly against
`FlatFeeTariffV2.calculateCost(LocalDateTime, LocalDateTime)`. No Spring context or mocks are needed — the domain
model is a plain Java object.

## 2. File to Modify / Create

* **File Path:**
  `service/src/test/java/com/github/jenkaby/bikerental/tariff/domain/model/FlatFeeTariffV2Test.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import com.github.jenkaby.bikerental.tariff.RentalCostV2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
```

**Full class to create:**

```java
package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import com.github.jenkaby.bikerental.tariff.RentalCostV2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FlatFeeTariffV2 — calendar-day billing (FR-1)")
class FlatFeeTariffV2Test {

    private static final Money ISSUANCE_FEE = Money.of("10.00");

    private FlatFeeTariffV2 tariff() {
        return new FlatFeeTariffV2(
                1L, "Test Flat Fee", "description", "bicycle",
                "v1", LocalDate.of(2025, 1, 1), null, TariffV2Status.ACTIVE,
                ISSUANCE_FEE
        );
    }

    @Test
    @DisplayName("Scenario 1: overnight rental spanning two calendar dates charges 2 days")
    void overnightRental_chargesTwoCalendarDays() {
        LocalDateTime startAt = LocalDateTime.of(2026, 6, 1, 20, 0);
        LocalDateTime returnAt = LocalDateTime.of(2026, 6, 2, 8, 0);

        RentalCostV2 result = tariff().calculateCost(startAt, returnAt);

        assertThat(result.totalCost()).isEqualTo(Money.of("20.00"));
        var details = (BreakdownCostDetails.FlatFee.Details) result.calculationBreakdown().getParams();
        assertThat(details.days()).as("billable days").isEqualTo(2);
    }

    @Test
    @DisplayName("Scenario 2: same-day rental charges 1 day")
    void sameDayRental_chargesOneCalendarDay() {
        LocalDateTime startAt = LocalDateTime.of(2026, 6, 1, 8, 0);
        LocalDateTime returnAt = LocalDateTime.of(2026, 6, 1, 18, 0);

        RentalCostV2 result = tariff().calculateCost(startAt, returnAt);

        assertThat(result.totalCost()).isEqualTo(Money.of("10.00"));
        var details = (BreakdownCostDetails.FlatFee.Details) result.calculationBreakdown().getParams();
        assertThat(details.days()).as("billable days").isEqualTo(1);
    }

    @Test
    @DisplayName("Scenario 3: multi-day rental spanning three calendar dates charges 3 days")
    void multiDayRental_chargesThreeCalendarDays() {
        LocalDateTime startAt = LocalDateTime.of(2026, 6, 1, 20, 0);
        LocalDateTime returnAt = LocalDateTime.of(2026, 6, 3, 20, 0);

        RentalCostV2 result = tariff().calculateCost(startAt, returnAt);

        assertThat(result.totalCost()).isEqualTo(Money.of("30.00"));
        var details = (BreakdownCostDetails.FlatFee.Details) result.calculationBreakdown().getParams();
        assertThat(details.days()).as("billable days").isEqualTo(3);
    }

    @Test
    @DisplayName("Scenario 4: zero duration (startAt == returnAt) charges minimum 1 day")
    void zeroDuration_chargesMinimumOneDay() {
        LocalDateTime sameTime = LocalDateTime.of(2026, 6, 1, 10, 0);

        RentalCostV2 result = tariff().calculateCost(sameTime, sameTime);

        assertThat(result.totalCost()).isEqualTo(Money.of("10.00"));
        var details = (BreakdownCostDetails.FlatFee.Details) result.calculationBreakdown().getParams();
        assertThat(details.days()).as("billable days for zero duration").isEqualTo(1);
    }
}
```

> `Money.of("10.00")`, `Money.of("20.00")`, etc. are used throughout existing tests in the project. Verify the
> exact factory method signature in `Money.java` if the build reports an error.

## 4. Validation Steps

skip