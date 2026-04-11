# Task 005: Add `existsByRentalRefAndType` to `TransactionRepository` Port and `TransactionRepositoryAdapter`

> **Applied Skill:** `spring-boot-data-ddd` — Hexagonal architecture; the domain port owns the contract; the adapter
> delegates to the JPA repository.

## 1. Objective

Expose the existence-check capability through the domain `TransactionRepository` port and implement it in the
infrastructure adapter so that `FinanceFacadeImpl` can call it without depending on JPA directly.

## 2. File to Modify / Create (Part A — domain port)

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/domain/repository/TransactionRepository.java`
* **Action:** Modify Existing File

### Code to Add/Replace

**Imports Required:** None — all required types are already imported.

* **Location:** Inside the `TransactionRepository` interface, directly **after** the `findAllByRentalRefAndType` method
  declaration.

**Before:**

```java
    List<Transaction> findAllByRentalRefAndType(RentalRef rentalRef, TransactionType type);

    Page<Transaction> findTransactionHistory(CustomerRef customerId,
```

**After:**

```java
    List<Transaction> findAllByRentalRefAndType(RentalRef rentalRef, TransactionType type);

    boolean existsByRentalRefAndType(RentalRef rentalRef, TransactionType type);

    Page<Transaction> findTransactionHistory(CustomerRef customerId,
```

---

## 3. File to Modify / Create (Part B — adapter)

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/adapter/TransactionRepositoryAdapter.java`
* **Action:** Modify Existing File

### Code to Add/Replace

**Imports Required:** None — all required types are already imported.

* **Location:** Inside `TransactionRepositoryAdapter`, directly **after** the `findAllByRentalRefAndType` method
  implementation.

**Before:**

```java
    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> findTransactionHistory(CustomerRef customerId, TransactionHistoryFilter filter, PageRequest pageRequest) {
```

**After:**

```java
    @Override
    public boolean existsByRentalRefAndType(RentalRef rentalRef, TransactionType type) {
        return jpaRepository.existsBySourceTypeAndSourceIdAndTransactionType(
                TransactionSourceType.RENTAL,
                String.valueOf(rentalRef.id()),
                type);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> findTransactionHistory(CustomerRef customerId, TransactionHistoryFilter filter, PageRequest pageRequest) {
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
