# Task 004: Create `GetCustomersByIdsService` Implementation

> **Applied Skill:** N/A — package-private `@Service` class following the established service pattern of
> the `customer` module (matches the structure of `GetCustomerByIdService` / `GetEquipmentByIdsService`).

## 1. Objective

Create the `GetCustomersByIdsService` application-service class that implements `GetCustomersByIdsUseCase`.
The class delegates directly to `CustomerRepository.findByIds()` with no additional business logic.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/customer/application/service/GetCustomersByIdsService.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

(All required types are included in the snippet below.)

**Code to Add/Replace:**

* **Location:** New file — paste the entire content as-is.

* **Snippet:**

```java
package com.github.jenkaby.bikerental.customer.application.service;

import com.github.jenkaby.bikerental.customer.application.usecase.GetCustomersByIdsUseCase;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
class GetCustomersByIdsService implements GetCustomersByIdsUseCase {

    private final CustomerRepository repository;

    GetCustomersByIdsService(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Customer> execute(List<UUID> ids) {
        return repository.findByIds(ids);
    }
}
```

## 4. Validation Steps

skip