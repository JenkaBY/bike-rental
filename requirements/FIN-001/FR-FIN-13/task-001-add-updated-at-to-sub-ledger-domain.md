# Task 001: Add `updatedAt` to `SubLedger` Domain Model

> **Applied Skill:** `spring-boot-data-ddd` — domain model extension; expose JPA-managed audit field to the
> domain layer so the application service can compute `lastUpdatedAt` without touching infrastructure directly.

## 1. Objective

`SubLedgerJpaEntity` already carries an `updatedAt` (`Instant`) column managed by `@LastModifiedDate`. This task
exposes it through the domain model (`SubLedger`) so that `GetCustomerAccountBalancesService` (Task 003) can
compute the `lastUpdatedAt` response field. `AccountJpaMapper.toDomain()` will auto-map the field once it exists
on `SubLedger`; `toEntity()` already ignores it (JPA manages it via Spring Data Auditing — no change needed there).

## 2. File to Modify

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/model/SubLedger.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import java.time.Instant;
```

**Code to Add/Replace:**

* **Location:** Inside the `SubLedger` class body, add the new field **after** the `version` field declaration
  (line `private Long version;`).

Current code:

```java
    private Long version;
```

Replace with:

```java
    private Long version;
    private Instant updatedAt;
```

The final class fields section must look exactly like this:

```java
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class SubLedger {

    private final UUID id;
    private final LedgerType ledgerType;
    @Setter(AccessLevel.PRIVATE)
    private Money balance;
    private Long version;
    private Instant updatedAt;
```

**No other changes are required in this file.** The `credit()` and `debit()` methods do not update `updatedAt`
in the domain model — the JPA `@LastModifiedDate` on `SubLedgerJpaEntity` handles this automatically on every
`save()` call.

**`AccountJpaMapper` impact (no code change required):**

`AccountJpaMapper.toDomain(SubLedgerJpaEntity entity)` does not have an explicit `@Mapping` for `updatedAt`.
MapStruct auto-maps because both source (`SubLedgerJpaEntity.updatedAt: Instant`) and target
(`SubLedger.updatedAt: Instant`) share the same name and type. The build will succeed without any mapper change.

`AccountJpaMapper.toEntity(SubLedger domain)` already declares `@Mapping(target = "updatedAt", ignore = true)`,
so the new source field is correctly ignored. No change needed.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Expected: build succeeds with no MapStruct unmapped-target errors.
