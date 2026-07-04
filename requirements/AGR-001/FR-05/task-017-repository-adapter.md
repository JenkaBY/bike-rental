<task_file_template>

# Task 017: Create the AgreementSignatureRepositoryAdapter

> **Applied Skill:** `spring-boot-data-ddd` — the persistence adapter implements the domain port, `@Repository`
> `@Transactional(readOnly = true)` at class level with a `@Transactional` write override on `save` (mirrors
> `AgreementTemplateRepositoryAdapter`). Delegates summary/PDF reads straight to the Spring Data repository. Zero inline
> comments. Depends on Tasks 007, 015, 016.

## 1. Objective

Create the adapter binding the `AgreementSignatureRepository` port to the JPA repository + mapper.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/infrastructure/persistence/adapter/AgreementSignatureRepositoryAdapter.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignature;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementSignatureRepository;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.mapper.AgreementSignatureJpaMapper;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.repository.AgreementSignatureJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
class AgreementSignatureRepositoryAdapter implements AgreementSignatureRepository {

    private final AgreementSignatureJpaRepository repository;
    private final AgreementSignatureJpaMapper mapper;

    AgreementSignatureRepositoryAdapter(AgreementSignatureJpaRepository repository,
                                        AgreementSignatureJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AgreementSignature save(AgreementSignature signature) {
        var entity = mapper.toEntity(signature);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByRentalId(Long rentalId) {
        return repository.existsByRentalId(rentalId);
    }

    @Override
    public Optional<AgreementSignatureSummary> findSummaryByRentalId(Long rentalId) {
        return repository.findSummaryByRentalId(rentalId);
    }

    @Override
    public Optional<byte[]> findPdfByRentalId(Long rentalId) {
        return repository.findPdfByRentalId(rentalId);
    }
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
