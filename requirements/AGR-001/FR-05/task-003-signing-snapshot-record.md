<task_file_template>

# Task 003: Create the SigningSnapshot domain value object

> **Applied Skill:** `java-best-practices` — records for immutable data carriers; zero inline comments; nested records
> for grouped data. This record is persisted as JSONB (Task 015), so it must be a plain immutable record with no
> framework imports.

## 1. Objective

Create the `SigningSnapshot` record capturing exactly what went into the signed PDF: the customer identity, the rental
data (with the informational rental version), the equipment lines, and the template reference. It is stored as the
`signing_snapshot` JSONB column.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/model/SigningSnapshot.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record SigningSnapshot(
        Customer customer,
        Rental rental,
        Template template) {

    public record Customer(String firstName, String lastName, String phone) {
    }

    public record Rental(Long rentalId,
                         Long rentalVersion,
                         Duration plannedDuration,
                         LocalDateTime startedAt,
                         List<EquipmentLine> equipments,
                         BigDecimal estimatedTotal) {
    }

    public record EquipmentLine(String uid, String name, BigDecimal estimatedCost) {
    }

    public record Template(Long templateId, Integer versionNumber, String contentSha256) {
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
