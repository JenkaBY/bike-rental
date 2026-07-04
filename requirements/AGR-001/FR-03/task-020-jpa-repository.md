<task_file_template>

# Task 020: Create the AgreementTemplateJpaRepository

> **Applied Skill:** `spring-boot-data-ddd` — Spring Data repository with a JPQL constructor
> projection so the heavy `content` TEXT column is never selected for the catalog list; `@Query` with
> `coalesce(max(...), 0)` for the next version number. Mirrors
> `tariff/.../TariffV2JpaRepository.java` (`@Query`) and `rental/.../RentalJpaRepository.java`.

## 1. Objective

Provide the Spring Data repository: find-by-status, max-version-number, and a summary projection query
that constructs `AgreementTemplateSummary` directly (NO `content` column). Depends on Task 019
(entity), Task 006 (summary record), Task 004 (status enum).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/infrastructure/persistence/repository/AgreementTemplateJpaRepository.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementTemplateJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AgreementTemplateJpaRepository extends JpaRepository<AgreementTemplateJpaEntity, Long> {

    Optional<AgreementTemplateJpaEntity> findByStatus(AgreementTemplateStatus status);

    @Query("select coalesce(max(t.versionNumber), 0) from AgreementTemplateJpaEntity t")
    int findMaxVersionNumber();

    @Query("""
            select new com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary(
                t.id, t.versionNumber, t.title, t.status, t.createdAt, t.activatedAt, t.deactivatedAt)
            from AgreementTemplateJpaEntity t
            order by t.id desc""")
    List<AgreementTemplateSummary> findAllSummaries();
}
```

> The constructor-projection JPQL selects only the seven summary columns; the `content` TEXT column is
> never fetched by construction (the NFR). The projection targets the DOMAIN record
> `AgreementTemplateSummary` directly, so no separate infrastructure projection type is needed. The
> `AgreementTemplateSummary` component order (id, versionNumber, title, status, createdAt, activatedAt,
> deactivatedAt) MUST match the `new (...)` argument order exactly.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
