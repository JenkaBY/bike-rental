<task_file_template>

# Task 013: Create the ActivateAgreementTemplateService (critical ordering)

> **Applied Skill:** `spring-boot-best-practices` — single `@Transactional` method, `Clock` injected
> (never `Instant.now()` in a service). `spring-boot-data-ddd` — the two-step flush is expressed via the
> port's `saveNow`, keeping `saveAndFlush` in the adapter. `java-best-practices` — `final` fields, zero
> inline comments. Mirrors the `Clock` injection in
> `finance/application/service/RecordDepositService.java` (`clock.instant()`).

## 1. Objective

Implement the activation transition with the DB-safe ordering: load draft (404 if absent), deactivate
the current active FIRST with an immediate flush, THEN activate the draft. Losing a concurrent race
surfaces as `DataIntegrityViolationException` / `ObjectOptimisticLockingFailureException` (both → 409,
handled by advices). Depends on Task 008 (port), Task 007 (aggregate), Task 009 (`ContentHasher`),
Task 010 (use case).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/application/service/ActivateAgreementTemplateService.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.ActivateAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@Service
class ActivateAgreementTemplateService implements ActivateAgreementTemplateUseCase {

    private final AgreementTemplateRepository repository;
    private final ContentHasher contentHasher;
    private final Clock clock;

    ActivateAgreementTemplateService(AgreementTemplateRepository repository,
                                     ContentHasher contentHasher,
                                     Clock clock) {
        this.repository = repository;
        this.contentHasher = contentHasher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AgreementTemplate execute(Long id) {
        var draft = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AgreementTemplate.class, id.toString()));

        Instant now = clock.instant();

        repository.findActive().ifPresent(current -> {
            current.deactivate(now);
            repository.saveNow(current);
            log.info("Deactivated previously active agreement template {}", current.getId());
        });

        draft.activate(repository.nextVersionNumber(), contentHasher.sha256(draft.getContent()), now);
        var activated = repository.save(draft);
        log.info("Activated agreement template {} as version {}", activated.getId(), activated.getVersionNumber());
        return activated;
    }
}
```

> ORDERING IS LOAD-BEARING. `current.deactivate(now)` + `repository.saveNow(current)` MUST run before
> `draft.activate(...)` + `repository.save(draft)`: the immediate UPDATE clears the ACTIVE row so the
> non-deferrable partial unique index `uq_agreement_templates_single_active` is never violated by the
> new ACTIVE row within the same transaction. Do NOT reorder these statements and do NOT replace
> `clock.instant()` with `Instant.now()`.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
