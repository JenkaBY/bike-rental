<task_file_template>

# Task 021: Create the AgreementTemplateJpaMapper

> **Applied Skill:** `mapstruct-hexagonal` — MapStruct interface maps domain aggregate ↔ JPA entity in
> the infrastructure layer; component model `spring`, `unmappedTargetPolicy=ERROR` (every target field
> must map). Mirrors `rental/infrastructure/persistence/mapper/RentalJpaMapper.java`.

## 1. Objective

Map `AgreementTemplate` (domain) ↔ `AgreementTemplateJpaEntity` (persistence). Field names are
identical on both sides (`id`, `lockVersion`, `versionNumber`, `title`, `content`, `contentSha256`,
`status`, `createdAt`, `updatedAt`, `activatedAt`, `deactivatedAt`), so no `@Mapping` overrides are
needed. Depends on Task 007 (aggregate) and Task 019 (entity).

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/infrastructure/persistence/mapper/AgreementTemplateJpaMapper.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementTemplateJpaEntity;
import org.mapstruct.Mapper;

@Mapper
public interface AgreementTemplateJpaMapper {

    AgreementTemplateJpaEntity toEntity(AgreementTemplate template);

    AgreementTemplate toDomain(AgreementTemplateJpaEntity entity);
}
```

> Both types carry `Instant` timestamps and the same `AgreementTemplateStatus` enum, so MapStruct maps
> them directly. A successful compile proves all target fields are covered under
> `unmappedTargetPolicy=ERROR`.

## 4. Validation Steps

Execute the following command — MapStruct generates the implementation at compile time.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
