# Task 002: Implement `findByIds` in `CustomerRepositoryAdapter`

> **Applied Skill:** `spring-boot-data-ddd` — infrastructure adapter implements domain port; uses
> inherited `JpaRepository.findAllById()` for a single `IN`-predicate query; maps each JPA entity to a
> domain object via the existing `CustomerJpaMapper`.

## 1. Objective

Add the `findByIds(Collection<UUID> ids)` implementation to `CustomerRepositoryAdapter`. The implementation
delegates to `CustomerJpaRepository.findAllById()` — a Spring Data method inherited from `JpaRepository`
that issues a single `SELECT … WHERE id IN (…)` query — and maps each returned entity to a domain
`Customer` via the existing `CustomerJpaMapper.toDomain()`.

No changes to `CustomerJpaRepository` are required: the `findAllById(Iterable<ID>)` method is already
provided by the inherited `JpaRepository<CustomerJpaEntity, UUID>`.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/customer/infrastructure/persistence/adapter/CustomerRepositoryAdapter.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import java.util.Collection;
```

(`java.util.List`, `java.util.Optional`, and `java.util.UUID` are already imported.)

**Code to Add/Replace:**

* **Location:** Inside the `CustomerRepositoryAdapter` class body, immediately after the existing
  `findById(UUID id)` method implementation.

* **Current code (context lines):**

```java
    @Override
    public Optional<Customer> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
```

* **Snippet (replace with):**

```java
    @Override
    public Optional<Customer> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Customer> findByIds(Collection<UUID> ids) {
        return repository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Customer> findByPhone(String phone) {
```

## 4. Validation Steps

skip
