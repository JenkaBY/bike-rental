<task_file_template>

# Task 022: Create the AgreementTemplateRepositoryAdapter

> **Applied Skill:** `spring-boot-data-ddd` — the infrastructure adapter implements the domain port and
> hides JPA details; `saveNow` → `saveAndFlush` (immediate DB synchronization). `java-best-practices` —
> package-private class, `final` fields, zero inline comments. Mirrors
> `rental/infrastructure/persistence/adapter/RentalRepositoryAdapter.java`.

## 1. Objective

Implement `AgreementTemplateRepository` on top of the Spring Data repository + MapStruct mapper. The
critical `saveNow` uses `saveAndFlush` so the ACTIVE→DEACTIVATED update hits the DB before the new
ACTIVE row is written (see Task 013). Depends on Task 008 (port), Task 020 (Spring Data repo), Task 021
(mapper), Task 006 (summary).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/infrastructure/persistence/adapter/AgreementTemplateRepositoryAdapter.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.mapper.AgreementTemplateJpaMapper;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.repository.AgreementTemplateJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
class AgreementTemplateRepositoryAdapter implements AgreementTemplateRepository {

    private final AgreementTemplateJpaRepository repository;
    private final AgreementTemplateJpaMapper mapper;

    AgreementTemplateRepositoryAdapter(AgreementTemplateJpaRepository repository, AgreementTemplateJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AgreementTemplate save(AgreementTemplate template) {
        var entity = mapper.toEntity(template);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public AgreementTemplate saveNow(AgreementTemplate template) {
        var entity = mapper.toEntity(template);
        var saved = repository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AgreementTemplate> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<AgreementTemplate> findActive() {
        return repository.findByStatus(AgreementTemplateStatus.ACTIVE).map(mapper::toDomain);
    }

    @Override
    public List<AgreementTemplateSummary> findAllSummaries() {
        return repository.findAllSummaries();
    }

    @Override
    public int nextVersionNumber() {
        return repository.findMaxVersionNumber() + 1;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
