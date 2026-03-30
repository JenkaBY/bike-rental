# Task 003: Update `TransactionJpaEntity` — Make `paymentMethod` Nullable, Add `reason` Column

> **Applied Skill:** `spring-boot-data-ddd` — JPA entity mapping rules; `mapstruct-hexagonal` — Pattern 4a (ignore
> audit fields; `@InheritInverseConfiguration` auto-maps same-named fields without explicit `@Mapping`).

## 1. Objective

Align the JPA entity with the updated domain model:

1. Remove the `nullable = false` constraint on `payment_method` so `ADJUSTMENT` transactions (which carry no payment
   method) can be persisted.
2. Add a nullable `reason` column that MapStruct's `@InheritInverseConfiguration` will auto-map from
   `Transaction.reason` without any changes to `TransactionJpaMapper`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/entity/TransactionJpaEntity.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import org.jspecify.annotations.Nullable;
```

(already imported — confirm before adding)

**Code to Add/Replace — change 1: make `paymentMethod` nullable**

* **Location:** The `paymentMethod` field annotation inside `TransactionJpaEntity`.

Replace:

```java
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;
```

With:

```java
    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;
```

**Code to Add/Replace — change 2: add `reason` field**

* **Location:** After the `idempotencyKey` field and before the `@OneToMany records` field.

Replace:

```java
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransactionRecordJpaEntity> records = new ArrayList<>();
```

With:

```java
    @Column(name = "idempotency_key", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Nullable
    @Column(name = "reason", length = 1000)
    private String reason;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransactionRecordJpaEntity> records = new ArrayList<>();
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
