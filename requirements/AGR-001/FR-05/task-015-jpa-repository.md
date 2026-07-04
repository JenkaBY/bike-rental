<task_file_template>

# Task 015: Create the AgreementSignatureJpaRepository

> **Applied Skill:** `spring-boot-data-ddd` — a Spring Data repository; the summary is built by a JPQL constructor
> expression selecting ONLY the summary columns (BYTEA never loaded); the PDF is fetched by a dedicated single-column
> `@Query` returning `Optional<byte[]>`. The `versionNumber` is joined from `AgreementTemplateJpaEntity` on
> `t.id = s.templateId` (the signature stores a plain `templateId`, not a relation). Depends on Task 005, Task 014.

## 1. Objective

Create the Spring Data JPA repository with: `existsByRentalId`, a summary constructor-expression query joining the
template for the version number, and a single-column PDF fetch by rental id.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/infrastructure/persistence/repository/AgreementSignatureJpaRepository.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.repository;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementSignatureJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AgreementSignatureJpaRepository extends JpaRepository<AgreementSignatureJpaEntity, Long> {

    boolean existsByRentalId(Long rentalId);

    @Query("""
            select new com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary(
                s.id, s.templateId, t.versionNumber, s.signedAt)
            from AgreementSignatureJpaEntity s
            join com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementTemplateJpaEntity t
                on t.id = s.templateId
            where s.rentalId = :rentalId""")
    Optional<AgreementSignatureSummary> findSummaryByRentalId(@Param("rentalId") Long rentalId);

    @Query("select s.pdfDocument from AgreementSignatureJpaEntity s where s.rentalId = :rentalId")
    Optional<byte[]> findPdfByRentalId(@Param("rentalId") Long rentalId);
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
