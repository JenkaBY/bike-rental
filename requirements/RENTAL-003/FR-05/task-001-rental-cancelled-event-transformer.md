# Task 001: Create RentalCancelledEventTransformer

> **Applied Skill:** `spring-boot-java-cucumber/SKILL.md` — `@DataTableType` transformers,
> `Aliases` utility, `DataTableHelper`

## 1. Objective

Create a Cucumber `@DataTableType` transformer that converts a datatable row into a
`RentalCancelled` event record. Follows the same pattern as `RentalCreatedEventTransformer` and
`RentalCompletedEventTransformer`.

## 2. File to Modify / Create

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/RentalCancelledEventTransformer.java`
* **Action:** Create New File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCancelled;
import io.cucumber.java.DataTableType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
```

**Code to Add/Replace:**

```java
package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCancelled;
import io.cucumber.java.DataTableType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RentalCancelledEventTransformer {

    @DataTableType
    public RentalCancelled transform(Map<String, String> entry) {
        var rentalId = Optional.ofNullable(DataTableHelper.getStringOrNull(entry, "rentalId"))
                .map(Long::valueOf)
                .orElse(null);

        var customerIdString = DataTableHelper.getStringOrNull(entry, "customerId");
        var customerId = Optional.ofNullable(customerIdString)
                .map(Aliases::getCustomerId)
                .orElse(null);

        var eqIdsString = DataTableHelper.getStringOrNull(entry, "equipmentIds");
        List<Long> equipmentIds = Optional.ofNullable(eqIdsString)
                .map(s -> Arrays.stream(s.split(","))
                        .map(String::trim)
                        .map(Long::valueOf)
                        .toList())
                .orElse(List.of());

        return new RentalCancelled(rentalId, customerId, equipmentIds);
    }
}
```

## 4. Validation Steps

skip