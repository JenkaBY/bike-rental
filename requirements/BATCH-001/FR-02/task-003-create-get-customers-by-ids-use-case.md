# Task 003: Create `GetCustomersByIdsUseCase` Interface

> **Applied Skill:** N/A — plain Java interface following the established use-case contract pattern of the
> `customer` module (matches the structure of `GetCustomerByIdUseCase`).

## 1. Objective

Create the `GetCustomersByIdsUseCase` application-layer contract so that `CustomerQueryController` depends
on an interface rather than the service implementation. The interface exposes a single method that accepts
a de-duplicated list of UUIDs and returns the matching domain `Customer` objects.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/customer/application/usecase/GetCustomersByIdsUseCase.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

(All required types are included in the snippet below.)

**Code to Add/Replace:**

* **Location:** New file — paste the entire content as-is.

* **Snippet:**

```java
package com.github.jenkaby.bikerental.customer.application.usecase;

import com.github.jenkaby.bikerental.customer.domain.model.Customer;

import java.util.List;
import java.util.UUID;

public interface GetCustomersByIdsUseCase {

    List<Customer> execute(List<UUID> ids);
}
```

## 4. Validation Steps

skip
