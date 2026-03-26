# Task 007: MapStruct JPA Mapper — AccountJpaMapper

> **Applied Skill:** `mapstruct-hexagonal/SKILL.md` — Infrastructure-layer mapper in
`infrastructure/persistence/mapper/`; no enum conversion expressions needed because both domain objects and JPA entities
> use the same enum types; `@AfterMapping` to fix up bi-directional JPA relationship (mirrors `RentalJpaMapper`).

## 1. Objective

Create `AccountJpaMapper` — a MapStruct interface in the finance module's infrastructure persistence mapper package.
It maps between the domain `Account` / `SubLedger` objects and their JPA entity counterparts. Because
`AccountJpaEntity` and `SubLedgerJpaEntity` use `@Enumerated(EnumType.STRING)` with the actual domain enum types
(`AccountType`, `LedgerType`), MapStruct maps those fields directly with no `expression` annotations required.
The `@AfterMapping` fixes back-references from `SubLedgerJpaEntity.account` to `AccountJpaEntity`, mirroring the
`RentalJpaMapper` approach.

## 2. File to Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/mapper/AccountJpaMapper.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.SubLedger;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.SubLedgerJpaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
```

**Code to Add/Replace:**

* **Location:** New file — full interface body.

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.finance.domain.model.Account;
import com.github.jenkaby.bikerental.finance.domain.model.SubLedger;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.SubLedgerJpaEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface AccountJpaMapper {

  // Note: `Account` uses `CustomerRef` in the domain; a `CustomerRefMapper` (componentModel = spring)
  // is used by the implementation to convert between `UUID` (entity.customerId) and `CustomerRef`.
  @org.mapstruct.Mapping(target = "customerRef", source = "customerId")
    Account toDomain(AccountJpaEntity entity);

  // When mapping domain->entity we intentionally ignore audit fields so JPA auditing can populate them.
  @org.mapstruct.Mapping(target = "customerId", source = "customerRef")
  @org.mapstruct.Mapping(target = "createdAt", ignore = true)
  @org.mapstruct.Mapping(target = "updatedAt", ignore = true)
    AccountJpaEntity toEntity(Account domain);

    @AfterMapping
    default void setSubLedgerRelationships(Account domain, @MappingTarget AccountJpaEntity result) {
        if (result.getSubLedgers() != null) {
            result.getSubLedgers().forEach(sl -> sl.setAccount(result));
        }
    }

    SubLedger toDomain(SubLedgerJpaEntity entity);

    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SubLedgerJpaEntity toEntity(SubLedger domain);
}
```

> **Why no `@Mapping` expressions for enum fields?**  
> `AccountJpaEntity.accountType` is `AccountType` and `SubLedgerJpaEntity.ledgerType` is `LedgerType` — the same
> types as on the domain side. MapStruct maps identical types with matching names automatically. The old
> `valueOf`/`name()` expressions were only needed when the JPA side held a `String`.

> **Why `@Mapping(target = "account", ignore = true)` on `toEntity(SubLedger)`?**  
> The `account` back-reference on `SubLedgerJpaEntity` cannot be populated during MapStruct's forward mapping
> because the parent `AccountJpaEntity` does not exist yet at that point. The `@AfterMapping` on `toEntity(Account)`
> sets all `.account` references after the full object graph is constructed — matching the `RentalJpaMapper`
> approach.
> sets all `.account` references after the full object graph is constructed — matching the `RentalJpaMapper`
> approach.

## 4. Validation Steps

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```
