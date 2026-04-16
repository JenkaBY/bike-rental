# Task 002: Add `toResponse` mapping method in `CustomerQueryMapper`

> **Applied Skill:** mapstruct-hexagonal - add Web Query mapper in `web/query/mapper` and follow MapStruct conventions

## 1. Objective

Expose a mapping method to convert `CustomerInfo` -> `CustomerResponse` used by the new controller endpoint.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/customer/web/query/mapper/CustomerQueryMapper.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import com.github.jenkaby.bikerental.customer.CustomerInfo;
```

**Code to Add/Replace:**

* **Location:** Add a new mapping method in the `CustomerQueryMapper` interface (next to `toSearchResponse(...)`).
* **Snippet:**

```java
    CustomerResponse toResponse(CustomerInfo customerInfo);
```

MapStruct will generate the implementation. If `CustomerInfo` does not yet contain `comments`, see Task 003.

## 4. Validation Steps

Build the service module to ensure MapStruct compiles and no unmapped-target errors appear:

```bash
./gradlew :service:build
```
