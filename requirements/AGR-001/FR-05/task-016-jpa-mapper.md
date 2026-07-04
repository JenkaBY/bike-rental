<task_file_template>

# Task 016: Create the AgreementSignatureJpaMapper

> **Applied Skill:** `mapstruct-hexagonal` — a `@Mapper` interface converting entity ↔ domain aggregate (mirrors
> `AgreementTemplateJpaMapper`). The `SigningSnapshot` and `byte[]` fields map by name automatically. Depends on Task
> 004, Task 014.

## 1. Objective

Create the MapStruct mapper between `AgreementSignature` (domain) and `AgreementSignatureJpaEntity`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/agreement/infrastructure/persistence/mapper/AgreementSignatureJpaMapper.java`
* **Action:** Create New File

## 3. Code Implementation

Create the file with EXACTLY this content:

```java
package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignature;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementSignatureJpaEntity;
import org.mapstruct.Mapper;

@Mapper
public interface AgreementSignatureJpaMapper {

    AgreementSignatureJpaEntity toEntity(AgreementSignature signature);

    AgreementSignature toDomain(AgreementSignatureJpaEntity entity);
}
```

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
