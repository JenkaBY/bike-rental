<task_file_template>

# Task 009: Create the `PreviewAgreementPdfService` (fixture assembly + delegation)

> **Applied Skill:** `spring-boot-best-practices` — `@Service`, constructor injection, `Clock` injected (never
> `LocalDateTime.now()` directly). Mirrors the `Clock` injection in `ActivateAgreementTemplateService`. No transaction
> (stateless, no DB access). `.claude/rules/java-style.md` — `final` fields, records for fixtures, zero inline comments.

## 1. Objective

Implement `PreviewAgreementPdfUseCase`: build an `AgreementPdfData` from the command plus hard-coded Cyrillic fixture
data (sample customer, two sample equipment lines, `startedAt = LocalDateTime.now(clock)`, 2h duration, `null`
signature ⇒ placeholder), then delegate to `AgreementPdfRenderer`. Depends on Task 003 (data), Task 004 (port), Task
008 (use case).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/application/service/PreviewAgreementPdfService.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.PreviewAgreementPdfUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;
import com.github.jenkaby.bikerental.agreement.domain.service.AgreementPdfRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
class PreviewAgreementPdfService implements PreviewAgreementPdfUseCase {

    private static final AgreementPdfData.CustomerData FIXTURE_CUSTOMER =
            new AgreementPdfData.CustomerData("Иван", "Иванов", "+375291234567");
    private static final Long FIXTURE_RENTAL_ID = 0L;
    private static final Duration FIXTURE_DURATION = Duration.ofHours(2);
    private static final List<AgreementPdfData.EquipmentLine> FIXTURE_EQUIPMENTS = List.of(
            new AgreementPdfData.EquipmentLine("BIKE-001", "Горный велосипед", new BigDecimal("25.00")),
            new AgreementPdfData.EquipmentLine("HELM-014", "Шлем защитный", new BigDecimal("5.00")));

    private final AgreementPdfRenderer renderer;
    private final Clock clock;

    PreviewAgreementPdfService(AgreementPdfRenderer renderer, Clock clock) {
        this.renderer = renderer;
        this.clock = clock;
    }

    @Override
    public byte[] execute(PreviewAgreementPdfCommand command) {
        log.info("Rendering agreement preview PDF for title '{}'", command.title());
        var rental = new AgreementPdfData.RentalData(
                FIXTURE_RENTAL_ID,
                LocalDateTime.now(clock),
                FIXTURE_DURATION,
                FIXTURE_EQUIPMENTS);
        var data = new AgreementPdfData(
                command.title(),
                command.content(),
                FIXTURE_CUSTOMER,
                rental,
                null);
        return renderer.render(data);
    }
}
```

> `signaturePng` is passed as `null` (preview mode ⇒ placeholder rectangle). Do NOT add `@Transactional`. Use the
> injected `Clock`, never `LocalDateTime.now()` without it.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
