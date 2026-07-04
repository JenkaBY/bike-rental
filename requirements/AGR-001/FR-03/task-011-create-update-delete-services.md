<task_file_template>

# Task 011: Create the Create/Update/Delete agreement services

> **Applied Skill:** `spring-boot-best-practices` — `@Service`, constructor injection, `@Transactional`
> on mutating methods; `java-best-practices` — package-private classes, `final` fields, zero inline
> comments. Mirrors `customer/application/service/CreateCustomerService.java` (load → domain method →
> save) and `PrepareSigningService` (404 via `ResourceNotFoundException` when the aggregate is absent).

## 1. Objective

Implement the three simple command services: create a DRAFT, edit a DRAFT (`updateContent`), and
delete a DRAFT (`ensureDeletable`). Depends on Task 008 (port), Task 007 (aggregate), Task 010
(use cases).

## 2. Files to Modify / Create

Create THREE new files under
`service/src/main/java/com/github/jenkaby/bikerental/agreement/application/service/`.

## 3. Code Implementation

### File 1: `CreateAgreementTemplateService.java`

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.CreateAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
class CreateAgreementTemplateService implements CreateAgreementTemplateUseCase {

    private final AgreementTemplateRepository repository;

    CreateAgreementTemplateService(AgreementTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public AgreementTemplate execute(CreateAgreementTemplateCommand command) {
        var draft = AgreementTemplate.createDraft(command.title(), command.content());
        var saved = repository.save(draft);
        log.info("Created agreement template draft {}", saved.getId());
        return saved;
    }
}
```

### File 2: `UpdateAgreementTemplateService.java`

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.UpdateAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
class UpdateAgreementTemplateService implements UpdateAgreementTemplateUseCase {

    private final AgreementTemplateRepository repository;

    UpdateAgreementTemplateService(AgreementTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public AgreementTemplate execute(UpdateAgreementTemplateCommand command) {
        var template = repository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException(AgreementTemplate.class, command.id().toString()));
        template.updateContent(command.title(), command.content());
        var saved = repository.save(template);
        log.info("Updated agreement template {}", saved.getId());
        return saved;
    }
}
```

### File 3: `DeleteAgreementTemplateService.java`

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.DeleteAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
class DeleteAgreementTemplateService implements DeleteAgreementTemplateUseCase {

    private final AgreementTemplateRepository repository;

    DeleteAgreementTemplateService(AgreementTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void execute(Long id) {
        var template = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AgreementTemplate.class, id.toString()));
        template.ensureDeletable();
        repository.deleteById(id);
        log.info("Deleted agreement template {}", id);
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
