# Task 001: Create `AvailableEquipmentResponseTransformer`

> **Applied Skill:** `d:\Projects\private\bikerent\.github\skills\spring-boot-java-cucumber\SKILL.md` — DataTable
> transformer pattern; `@DataTableType` method named `transform`; uses `DataTableHelper` helpers; placed in
> `componenttest/transformer/` next to `EquipmentResponseTransformer` as the canonical reference.

## 1. Objective

Create a Cucumber `@DataTableType` transformer that converts a DataTable row map into an
`AvailableEquipmentResponse` object. This is required so that the new `Then the available equipment response
only contains page of` step (Task 002) can accept a typed list from the feature file's DataTable.

## 2. File to Modify / Create

* **File Path:**
  `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/transformer/AvailableEquipmentResponseTransformer.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** Already covered by the snippet below.

**Code to Add/Replace:**

* **Location:** New file — entire file content below.
* **Snippet:**

```java
package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.rental.web.query.dto.AvailableEquipmentResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class AvailableEquipmentResponseTransformer {

    @DataTableType
    public AvailableEquipmentResponse transform(Map<String, String> entry) {
        return new AvailableEquipmentResponse(
                DataTableHelper.toLong(entry, "id"),
                DataTableHelper.getStringOrNull(entry, "uid"),
                DataTableHelper.getStringOrNull(entry, "serialNumber"),
                DataTableHelper.getStringOrNull(entry, "typeSlug"),
                DataTableHelper.getStringOrNull(entry, "model")
        );
    }
}
```

> **Key rules:**
> - `AvailableEquipmentResponse` constructor parameter order is `(Long id, String uid, String serialNumber,
>   String typeSlug, String model)` — matches the record definition in
    > `rental/web/query/dto/AvailableEquipmentResponse.java` (from FR-06 task-003).
> - `DataTableHelper` is in the same package — no import required.
> - No `@Component` annotation; Cucumber discovers `@DataTableType` providers by classpath scan.

## 4. Validation Steps

ckip
