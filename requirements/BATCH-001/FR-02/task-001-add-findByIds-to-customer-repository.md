# Task 001: Add `findByIds` to `CustomerRepository` Domain Port

> **Applied Skill:** `spring-boot-data-ddd` — domain repository port is a plain Java interface; infrastructure
> concerns stay in the adapter; the port declares intent without referencing JPA or Spring Data.

## 1. Objective

Extend the `CustomerRepository` domain-port interface with a new `findByIds(Collection<UUID> ids)` method so
the application layer (`GetCustomersByIdsService`) can declare a dependency on batch lookup without coupling
to the JPA implementation.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/customer/domain/repository/CustomerRepository.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import java.util.Collection;
```

(`java.util.List`, `java.util.Optional`, and `java.util.UUID` are already imported.)

**Code to Add/Replace:**

* **Location:** Inside the `CustomerRepository` interface body, immediately after the existing
  `Optional<Customer> findById(UUID id);` method declaration.

* **Current code (context lines):**

```java
public interface CustomerRepository {
    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByPhone(String phone);
```

* **Snippet (replace with):**

```java
public interface CustomerRepository {
    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    List<Customer> findByIds(Collection<UUID> ids);

    Optional<Customer> findByPhone(String phone);
```

## 4. Validation Steps

skip
