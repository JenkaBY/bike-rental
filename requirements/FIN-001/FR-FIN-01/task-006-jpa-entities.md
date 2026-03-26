# Task 006: JPA Entities — SubLedgerJpaEntity & AccountJpaEntity

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — JPA entities with Lombok;
`@OneToMany(cascade = ALL, orphanRemoval = true)` with `@Fetch(FetchMode.SUBSELECT)` following the `RentalJpaEntity`
> pattern; `@ManyToOne(fetch = FetchType.LAZY)` on the child side.  
> **Applied Skill:** `java.instructions.md` — `TIMESTAMP WITH TIME ZONE` maps to `Instant`; all fields declared via
> constructor injection, no `@Value`.

## 1. Objective

Create the two JPA entity classes that mirror the `finance_accounts` and `finance_sub_ledgers` tables.
`AccountJpaEntity`
owns a `@OneToMany` cascade to `SubLedgerJpaEntity`. `SubLedgerJpaEntity` holds a `@ManyToOne(LAZY)` back-reference.
Both follow the exact Lombok annotation style used in `RentalJpaEntity` and `RentalEquipmentJpaEntity`.

## 2. Files to Create

### File 1 — SubLedgerJpaEntity

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/entity/SubLedgerJpaEntity.java`
* **Action:** Create New File

### File 2 — AccountJpaEntity

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/entity/AccountJpaEntity.java`
* **Action:** Create New File

---

## 3. Code Implementation

### SubLedgerJpaEntity.java

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
```

**Code to Add/Replace:**

* **Location:** New file — full class body.

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity;

import com.github.jenkaby.bikerental.finance.domain.model.LedgerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "finance_sub_ledgers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "account")
public class SubLedgerJpaEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountJpaEntity account;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_type", nullable = false, length = 30)
    private LedgerType ledgerType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

---

### AccountJpaEntity.java

**Imports Required:**

```java
import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
```

**Code to Add/Replace:**

* **Location:** New file — full class body.

```java
package com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity;

import com.github.jenkaby.bikerental.finance.domain.model.AccountType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "finance_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AccountJpaEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SubLedgerJpaEntity> subLedgers = new ArrayList<>();
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

Notes/Implementation details added during work:

- Entities keep `created_at` / `updated_at` columns. The implementation adds JPA auditing to automatically populate
  these columns on insert/update.

- Changes applied in codebase:
  - `@EntityListeners(AuditingEntityListener.class)` added to both `AccountJpaEntity` and `SubLedgerJpaEntity`.
  - `@CreatedDate` used on `createdAt` fields and `@LastModifiedDate` used on `updatedAt` where applicable.
  - `BikeRentalApplication` was annotated with `@EnableJpaAuditing` to enable auditing support.

Make sure the `spring-data-jpa` auditing support is available on the classpath (standard Spring Boot starter provides
it). Re-run the compile and tests after these changes.
