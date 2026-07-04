<task_file_template>

# Task 003: Create the `AgreementPdfData` domain record (renderer input)

> **Applied Skill:** `java-best-practices` / `.claude/rules/java-style.md` — records for immutable data carriers,
> zero inline comments, no framework imports in the domain layer.

## 1. Objective

Introduce the framework-free data carrier the PDF renderer consumes: agreement title/content, customer data, rental
data (with equipment lines), and an optional signature PNG. A `null` signature means "draw a placeholder" (preview
mode). This record has no dependency on Spring, JPA, or web types.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/domain/model/AgreementPdfData.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.domain.model;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record AgreementPdfData(
        String title,
        String content,
        CustomerData customer,
        RentalData rental,
        @Nullable byte[] signaturePng) {

    public record CustomerData(String firstName, String lastName, String phone) {
    }

    public record RentalData(Long rentalId,
                             LocalDateTime startedAt,
                             Duration plannedDuration,
                             List<EquipmentLine> equipments) {
    }

    public record EquipmentLine(String uid, String name, BigDecimal estimatedCost) {
    }
}
```

> `signaturePng` is deliberately nullable (`@Nullable`). Do NOT add a preview boolean flag — the null signature IS the
> preview signal. Do NOT import any Spring/JPA/web type here.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
