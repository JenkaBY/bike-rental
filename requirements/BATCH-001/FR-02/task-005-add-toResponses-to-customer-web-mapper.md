# Task 005: Add `toResponses` List-Mapping Method to `CustomerWebMapper`

> **Applied Skill:** `mapstruct-hexagonal` — Pattern 1 (Simple Interface Mapper); list mapping is derived
> automatically from the existing single-element `toResponse(Customer)` method; no additional `@Mapping`
> annotations or `uses = …` delegates are required.

## 1. Objective

Add a `List<CustomerResponse> toResponses(List<Customer> customers)` method to `CustomerWebMapper` so the
batch controller handler can convert the domain list into response DTOs in a single call. MapStruct will
auto-derive the implementation.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/customer/web/mapper/CustomerWebMapper.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None — `java.util.List` is already imported.

**Code to Add/Replace:**

* **Location:** Inside the `CustomerWebMapper` interface body, immediately after the existing
  `CustomerResponse toResponse(Customer customer);` method declaration.

* **Current code (context lines):**

```java
    CustomerResponse toResponse(Customer customer);

    CustomerSearchResponse toSearchResponse(CustomerInfo customerInfo);
```

* **Snippet (replace with):**

```java
    CustomerResponse toResponse(Customer customer);

    List<CustomerResponse> toResponses(List<Customer> customers);

    CustomerSearchResponse toSearchResponse(CustomerInfo customerInfo);
```

## 4. Validation Steps

skip