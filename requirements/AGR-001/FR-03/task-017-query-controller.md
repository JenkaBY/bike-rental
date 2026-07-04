<task_file_template>

# Task 017: Create the AgreementTemplateQueryController

> **Applied Skill:** `spring-boot-best-practices` — read-only controller delegating to query use cases.
> `java-best-practices` — zero inline comments. Mirrors
> `customer/web/query/CustomerQueryController.java`.

## 1. Objective

Expose the three query endpoints: `GET /api/agreements` (summary list), `GET /api/agreements/active`
(full DTO, 404 if none), `GET /api/agreements/{id}` (full DTO, 404 if absent). Depends on Task 010
(use cases), Task 014 (DTOs), Task 015 (mapper).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/query/AgreementTemplateQueryController.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.web.query;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindAgreementTemplateSummariesUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.GetActiveAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.GetAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.web.mapper.AgreementTemplateWebMapper;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateResponse;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateSummaryResponse;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping(path = "/api/agreements", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
class AgreementTemplateQueryController {

    private final FindAgreementTemplateSummariesUseCase findAgreementTemplateSummariesUseCase;
    private final GetActiveAgreementTemplateUseCase getActiveAgreementTemplateUseCase;
    private final GetAgreementTemplateUseCase getAgreementTemplateUseCase;
    private final AgreementTemplateWebMapper mapper;

    AgreementTemplateQueryController(FindAgreementTemplateSummariesUseCase findAgreementTemplateSummariesUseCase,
                                     GetActiveAgreementTemplateUseCase getActiveAgreementTemplateUseCase,
                                     GetAgreementTemplateUseCase getAgreementTemplateUseCase,
                                     AgreementTemplateWebMapper mapper) {
        this.findAgreementTemplateSummariesUseCase = findAgreementTemplateSummariesUseCase;
        this.getActiveAgreementTemplateUseCase = getActiveAgreementTemplateUseCase;
        this.getAgreementTemplateUseCase = getAgreementTemplateUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<AgreementTemplateSummaryResponse>> findAll() {
        log.info("[GET] Listing agreement template summaries");
        var summaries = findAgreementTemplateSummariesUseCase.execute();
        return ResponseEntity.ok(mapper.toSummaryResponses(summaries));
    }

    @GetMapping("/active")
    public ResponseEntity<AgreementTemplateResponse> getActive() {
        log.info("[GET] Fetching active agreement template");
        var active = getActiveAgreementTemplateUseCase.execute();
        return ResponseEntity.ok(mapper.toResponse(active));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgreementTemplateResponse> getById(@PathVariable("id") @Positive Long id) {
        log.info("[GET] Fetching agreement template {}", id);
        var template = getAgreementTemplateUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(template));
    }
}
```

> PATH-CONFLICT SAFEGUARD: the `/active` literal mapping is declared BEFORE `/{id}`, and `{id}` is a
> `Long` (not `String`), so Spring's handler mapping prefers the literal `/active` route and would fail
> type conversion for a non-numeric segment anyway. Do NOT change `{id}` to `String` and do NOT put
> `/{id}` before `/active`.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
