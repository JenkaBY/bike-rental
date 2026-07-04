<task_file_template>

# Task 012: Create the agreement query services

> **Applied Skill:** `spring-boot-best-practices` — read-only query services; `@Transactional(readOnly = true)`.
> `java-best-practices` — package-private classes, `final` fields, zero inline comments. `error-responses`
> rule — active-absent surfaces as `ActiveAgreementTemplateNotFoundException` (mapped to 404 by the advice).

## 1. Objective

Implement the three query services: get-by-id (404 if absent), find-all-summaries, and get-active
(throws `ActiveAgreementTemplateNotFoundException` when none). Depends on Task 008 (port), Task 010
(use cases), Task 005 (exception).

## 2. Files to Modify / Create

Create THREE new files under
`service/src/main/java/com/github/jenkaby/bikerental/agreement/application/service/`.

## 3. Code Implementation

### File 1: `GetAgreementTemplateService.java`

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.GetAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class GetAgreementTemplateService implements GetAgreementTemplateUseCase {

    private final AgreementTemplateRepository repository;

    GetAgreementTemplateService(AgreementTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public AgreementTemplate execute(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AgreementTemplate.class, id.toString()));
    }
}
```

### File 2: `FindAgreementTemplateSummariesService.java`

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindAgreementTemplateSummariesUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
class FindAgreementTemplateSummariesService implements FindAgreementTemplateSummariesUseCase {

    private final AgreementTemplateRepository repository;

    FindAgreementTemplateSummariesService(AgreementTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgreementTemplateSummary> execute() {
        return repository.findAllSummaries();
    }
}
```

### File 3: `GetActiveAgreementTemplateService.java`

```java
package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.GetActiveAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.exception.ActiveAgreementTemplateNotFoundException;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class GetActiveAgreementTemplateService implements GetActiveAgreementTemplateUseCase {

    private final AgreementTemplateRepository repository;

    GetActiveAgreementTemplateService(AgreementTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public AgreementTemplate execute() {
        return repository.findActive()
                .orElseThrow(ActiveAgreementTemplateNotFoundException::new);
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
