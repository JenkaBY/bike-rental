<task_file_template>

# Task 016: Create the AgreementTemplateCommandController

> **Applied Skill:** `spring-boot-best-practices` — thin `@RestController`, constructor injection,
> delegates to use cases. `java-best-practices` — zero inline comments. Mirrors
> `customer/web/command/CustomerCommandController.java` (package-private controller, `@Validated`,
> `@Valid @RequestBody`, `ResponseEntity.status(CREATED)`).

## 1. Objective

Expose the four command endpoints under `/api/agreements`: POST (201), PATCH `/{id}` (200), PATCH
`/{id}/activate` (200), DELETE `/{id}` (204). Depends on Task 010 (use cases), Task 014 (DTOs),
Task 015 (mapper).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/web/command/AgreementTemplateCommandController.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.web.command;

import com.github.jenkaby.bikerental.agreement.application.usecase.ActivateAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.CreateAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.DeleteAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.application.usecase.UpdateAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementTemplateRequest;
import com.github.jenkaby.bikerental.agreement.web.mapper.AgreementTemplateWebMapper;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(path = "/api/agreements", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
class AgreementTemplateCommandController {

    private final CreateAgreementTemplateUseCase createAgreementTemplateUseCase;
    private final UpdateAgreementTemplateUseCase updateAgreementTemplateUseCase;
    private final ActivateAgreementTemplateUseCase activateAgreementTemplateUseCase;
    private final DeleteAgreementTemplateUseCase deleteAgreementTemplateUseCase;
    private final AgreementTemplateWebMapper mapper;

    AgreementTemplateCommandController(CreateAgreementTemplateUseCase createAgreementTemplateUseCase,
                                       UpdateAgreementTemplateUseCase updateAgreementTemplateUseCase,
                                       ActivateAgreementTemplateUseCase activateAgreementTemplateUseCase,
                                       DeleteAgreementTemplateUseCase deleteAgreementTemplateUseCase,
                                       AgreementTemplateWebMapper mapper) {
        this.createAgreementTemplateUseCase = createAgreementTemplateUseCase;
        this.updateAgreementTemplateUseCase = updateAgreementTemplateUseCase;
        this.activateAgreementTemplateUseCase = activateAgreementTemplateUseCase;
        this.deleteAgreementTemplateUseCase = deleteAgreementTemplateUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<AgreementTemplateResponse> create(@Valid @RequestBody AgreementTemplateRequest request) {
        log.info("[POST] Creating agreement template draft");
        var command = new CreateAgreementTemplateUseCase.CreateAgreementTemplateCommand(request.title(), request.content());
        AgreementTemplate created = createAgreementTemplateUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AgreementTemplateResponse> update(@PathVariable("id") @Positive Long id,
                                                            @Valid @RequestBody AgreementTemplateRequest request) {
        log.info("[PATCH] Updating agreement template {}", id);
        var command = new UpdateAgreementTemplateUseCase.UpdateAgreementTemplateCommand(id, request.title(), request.content());
        AgreementTemplate updated = updateAgreementTemplateUseCase.execute(command);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<AgreementTemplateResponse> activate(@PathVariable("id") @Positive Long id) {
        log.info("[PATCH] Activating agreement template {}", id);
        AgreementTemplate activated = activateAgreementTemplateUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(activated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") @Positive Long id) {
        log.info("[DELETE] Deleting agreement template {}", id);
        deleteAgreementTemplateUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
