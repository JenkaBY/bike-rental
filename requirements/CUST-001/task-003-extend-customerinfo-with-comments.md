# Task 003: Add `comments` to `CustomerInfo` and ensure application mapper maps it

> **Applied Skill:** mapstruct-hexagonal (application mappers) + spring-boot-data-ddd (domain→application mapping
> conventions)

## 1. Objective

Include `comments` in the `CustomerInfo` projection so the new `CustomerResponse` can expose comments without changing
persistent schema.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/customer/CustomerInfo.java`
* **Action:** Modify Existing File

Additionally update any test factories that construct `CustomerInfo`.

## 3. Code Implementation

**Imports Required:**

```java
import java.time.LocalDate;
import java.util.UUID;
```

**Code to Add/Replace:**

* **Location:** Replace the `CustomerInfo` record declaration to include `comments` as the last field.
* **Snippet:**

```java
public record CustomerInfo(
        UUID id,
        String phone,
        String firstName,
        String lastName,
        String email,
        LocalDate birthDate,
        String comments
) {
}
```

**Also:** Search tests under `service/src/test/java` for `new CustomerInfo(...)` and update constructor usages to
provide `comments` (use `null` when not needed). For example update occurrences in:

- `service/src/test/java/com/github/jenkaby/bikerental/customer/web/query/CustomerQueryControllerTest.java`
- `service/src/test/java/com/github/jenkaby/bikerental/customer/application/service/CustomerQueryServiceTest.java`

Change e.g. `new CustomerInfo(id, phone, fn, ln, null, null)` → `new CustomerInfo(id, phone, fn, ln, null, null, null)`.

MapStruct will then map `Customer.comments` → `CustomerInfo.comments` automatically via
`CustomerMapper.toInfo(Customer)` (no change needed to `CustomerMapper` if field name matches).

## 4. Validation Steps

Compile the project and run the two unit tests that reference `CustomerInfo`:

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests com.github.jenkaby.bikerental.customer.web.query.CustomerQueryControllerTest
./gradlew :service:test "-Dspring.profiles.active=test" --tests com.github.jenkaby.bikerental.customer.application.service.CustomerQueryServiceTest
```
