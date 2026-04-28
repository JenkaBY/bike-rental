# System Design: CUST-002 — Paginated Customer List with Optional Phone Filter

## 1. Architectural Overview

The existing customer query layer currently supports phone-based searching via `GET /api/customers?phone={digits}`,
returning an unordered list without pagination. To support the new requirement—"get paged customers"—the system must
evolve the query endpoint to **optionally accept pagination parameters (page, size) and always apply lastName-based
sorting**. This enhancement affects three architectural layers: the **API controller** (to accept new query parameters),
the **application use-case** (to coordinate pagination logic), and the **repository adapter** (to execute paginated,
sorted queries). The phone filter remains optional and is applied before pagination, maintaining backward compatibility
while enabling operators to browse customers without a search term.

---

## 2. Impacted Components

* **`CustomerQueryController` (Web Layer)**
    - **Change:** Modify the existing `GET /api/customers` endpoint to accept:
        - Optional `phone` query parameter (filtered via `@Spec` annotation using specification-arg-resolver library)
        - `Specification<CustomerJpaEntity>` parameter (declaratively built from query params)
        - `Pageable` parameter with default lastName/firstName sorting
    - **Responsibility:** Spring Data JPA Specification annotations declaratively define filters; delegate to the
      use-case; map domain results to `CustomerPageResponse` DTO
    - **Note:** Refactor `searchByPhone()` method to use `Specification` pattern (similar to
      `TransactionRepositoryAdapter#findTransactionHistory`)

* **`CustomerQueryUseCase` (Application / Use-Case Interface)**
    - **Change:** Add new method for paginated customer queries using `Specification` pattern
    - **Responsibility:** Define contract for paginated customer queries with specification-based filtering; enforce
      business rules (sort by lastName)
    - **New Method:** `Page<CustomerInfo> findAllPaginated(Specification<CustomerJpaEntity> spec, Pageable pageable)`
    - **Pattern:** Accepts declarative `Specification` built from query parameters; sorting is embedded in `Pageable`

* **`CustomerQueryService` (Application Service Implementation)**
    - **Change:** Implement pagination logic using specification-based filtering
    - **Responsibility:** Accept `Specification<CustomerJpaEntity>` from controller; create `Pageable` with
      lastName+firstName sort; delegate to repository; return paginated result
    - **Sorting:** Always embed `Sort.by(Sort.Order.asc("lastName"), Sort.Order.asc("firstName"))` in the `Pageable`
    - **Filter Application:** Specification is declaratively defined (no manual WHERE clause construction)

* **`CustomerRepository` (Domain Port Interface)**
    - **Change:** Add new repository method supporting paginated queries with `Specification`
    - **Responsibility:** Define contract for database-level pagination and declarative filtering
    - **New Method:** `Page<Customer> findAllPaginated(Specification<CustomerJpaEntity> spec, Pageable pageable)`
    - **Pattern:** Accepts Spring Data JPA `Specification` for filter composition

* **`CustomerRepositoryAdapter` (Infrastructure / JPA Adapter)**
    - **Change:** Implement the new paginated repository method using Spring Data JPA `findAll(Specification, Pageable)`
    - **Responsibility:**
        - Accept `Specification<CustomerJpaEntity>` (declaratively defined by `@Spec` annotations)
        - Execute database queries via `jpaRepository.findAll(spec, pageable)` with WHERE, ORDER BY, and LIMIT/OFFSET
        - Map JPA results back to domain objects
    - **Implementation Pattern:** Follow `TransactionRepositoryAdapter#findTransactionHistory()`:
        - Create `Pageable` with `Sort.by(Sort.Direction.ASC, "lastName", "firstName")`
        - Invoke `jpaRepository.findAll(specification, pageable)`
        - Map `Page<CustomerJpaEntity>` to `Page<Customer>` using domain mapper

* **New DTO: `CustomerPageResponse` (Web / Response Layer)**
    - **New Component**
    - **Responsibility:** Wrap paginated results and metadata for JSON serialization
    - **Structure:** Contains `content` (list of `CustomerInfo`), `totalElements`, `totalPages`, `currentPage`,
      `pageSize`, `hasNext`, `hasPrevious`

* **New Mapper: `CustomerPageResponseMapper` (Application / Mapper Layer)**
    - **New Component**
    - **Responsibility:** Convert Spring Data `Page<CustomerInfo>` domain objects to `CustomerPageResponse` DTO
    - **Pattern:** Follow existing MapStruct mapper conventions

* **New Specification: `CustomerSpec` (Infrastructure / Specification Interface)**
    - **New Component**
    - **Responsibility:** Declaratively define customer filtering criteria using `@Spec` annotations (similar to
      `CustomerTransactionsSpec`)
    - **Configuration:**
      ```java
      @And({
          @Spec(path = "phone", params = "phone", spec = Like.class)
      })
      public interface CustomerSpec extends Specification<CustomerJpaEntity> {}
      ```
    - **Usage:** Controller method parameter accepts specification built from query parameters; specification is
      composable and reusable

---

## 3. Abstract Data Schema Changes

* **Entity: `Customer` (No changes)**
    * No new attributes required; existing fields (`id`, `phone`, `firstName`, `lastName`, `email`, `birthDate`,
      `comments`, `createdAt`) are sufficient
    * **Database Index:** Verify `phone` column has a B-tree index for efficient LIKE queries (assumed to exist from
      US-CL-001)

* **Logical View: Paginated Customer Result**
    * **New logical entity (DTO-level, not persisted):** `CustomerPageResponse`
    * **Attributes:**
        - `content`: List of `CustomerInfo` records
        - `totalElements`: Integer (total count of matching customers, not just current page)
        - `totalPages`: Integer (ceil(totalElements / pageSize))
        - `currentPage`: Integer (zero-indexed page number from request)
        - `pageSize`: Integer (records per page from request)
        - `hasNext`: Boolean (currentPage < totalPages - 1)
        - `hasPrevious`: Boolean (currentPage > 0)

---

## 4. Component Contracts & Payloads

* **Interaction: `CustomerQueryController` → `CustomerQueryUseCase`**
    * **Protocol:** Synchronous in-process method call
    * **Payload Changes:**
        - **Input:**
            - `Specification<CustomerJpaEntity>` (declaratively built by spring-data-jpa specification-arg-resolver from
              `@Spec` annotations on controller method)
            - `Pageable` (includes page, size, and Sort with lastName ASC, firstName ASC)
        - **Processing:** Service passes specifications to repository; repository applies filter, pagination, and
          sorting at database level
        - **Output:** `Page<CustomerInfo>` domain result from use-case

* **Interaction: `CustomerQueryUseCase` → `CustomerRepository`**
    * **Protocol:** Synchronous in-process method call (domain port invocation)
    * **Payload Changes:**
        - **Input:** `Specification<CustomerJpaEntity>`, `Pageable` with sort specification
        - **Processing:**
            - `SpecificationBuilder.specification(CustomerSpec.class)` builds composite specification from query
              parameters
            - Repository applies filter, pagination, and sorting via `jpaRepository.findAll(spec, pageable)`
            - Query pattern:
              ```sql
              SELECT * FROM customers 
              WHERE phone LIKE '%{phone}%' (conditional based on spec)
              ORDER BY last_name ASC, first_name ASC 
              LIMIT {size} OFFSET {page_offset}
              ```
        - **Output:** Spring Data JPA `Page<Customer>` with metadata (totalElements, totalPages, etc.)

* **Interaction: `CustomerQueryController` → `CustomerPageResponseMapper`**
    * **Protocol:** Synchronous in-process mapping/translation
    * **Payload Changes:**
        - **Input:** Spring Data `Page<CustomerInfo>` with metadata (totalElements, totalPages, number, size)
        - **Processing:** Copy pagination metadata, map customer records to response DTOs
        - **Output:** `CustomerPageResponse` DTO for JSON serialization

---

## 5. Updated Interaction Sequence

### **Happy Path: List All Customers with Pagination**

1. **HTTP Request:** Client calls `GET /api/customers?page=0&size=10`
2. **Controller Method Signature:**
   ```java
   @GetMapping
   public Page<CustomerResponse> listCustomers(
       @Spec(path = "phone", params = "phone", spec = Like.class) Specification<CustomerJpaEntity> phoneSpec,
       @PageableDefault(size = 10) Pageable pageable) { ... }
   ```
3. **Specification Building:** Spring's specification-arg-resolver automatically builds `phoneSpec` from query params (
   phone param absent, so spec is empty/no-op)
4. **Pageable Creation:** Spring's `@PageableDefault` provides
   `Pageable(page=0, size=10, sort=lastName ASC, firstName ASC)` (service must ensure sort is set)
5. **Use-Case Delegation:** Controller calls `customerQueryUseCase.findAllPaginated(phoneSpec, pageable)`
6. **Service Logic:** `CustomerQueryService` passes both to repository without modification
7. **Repository Query:** `CustomerRepositoryAdapter.findAllPaginated(phoneSpec, pageable)` internally executes:
   ```java
   var pageable = PageRequest.of(0, 10, Sort.by("lastName", "firstName"));
   jpaRepository.findAll(specification, pageable);  // Generates:
   ```
   ```sql
   SELECT * FROM customers 
   ORDER BY last_name ASC, first_name ASC 
   LIMIT 10 OFFSET 0
   ```
8. **Domain Result:** Repository returns `Page<Customer>` with 10 records, `totalElements = 25`, `totalPages = 3`
9. **Mapping:** `CustomerPageResponseMapper` converts `Page<CustomerInfo>` to `CustomerPageResponse`
10. **HTTP Response:** `200 OK` with paginated customer list and metadata

### **Happy Path: Filter by Phone + Paginate**

1. **HTTP Request:** Client calls `GET /api/customers?phone=1234&page=0&size=10`
2. **Specification Building:** Spring's specification-arg-resolver detects `phone=1234` and builds `Specification` using
   `CustomerSpec` annotations:
    - `@Spec(path = "phone", params = "phone", spec = Like.class)` creates: `phone LIKE '%1234%'`
3. **Validation:** Specification includes phone validation (4–11 digits); invalid format triggers
   `ConstraintViolationException`
4. **Use-Case Delegation:** Controller calls `customerQueryUseCase.findAllPaginated(phoneSpec, pageable)` where
   `phoneSpec` encodes the LIKE filter
5. **Service Logic:** Service passes specification to repository unchanged
6. **Repository Query:** `findAllPaginated(phoneSpec, pageable)` executes:
   ```sql
   SELECT * FROM customers 
   WHERE phone LIKE '%1234%'
   ORDER BY last_name ASC, first_name ASC 
   LIMIT 10 OFFSET 0
   ```
7. **Domain Result:** Repository returns `Page<Customer>` with 5 records (all matches), `totalElements = 5`,
   `totalPages = 1`
8. **Mapping & Response:** As above

### **Unhappy Path: Invalid Page Parameter**

1. **HTTP Request:** Client calls `GET /api/customers?page=abc&size=10`
2. **Spring Pageable Binding:** Spring's `Pageable` resolver fails to convert `page=abc` to integer
3. **Error Handling:** `MethodArgumentTypeMismatchException` is caught by global `CoreExceptionHandlerAdvice`
4. **Response:** `400 Bad Request` with `ProblemDetail`:
   ```json
   {
     "status": 400,
     "title": "Bad Request",
     "errorCode": "INVALID_REQUEST_PARAMETER",
     "correlationId": "uuid-...",
     "detail": "Failed to convert parameter 'page' from type 'String' to required type 'int'"
   }
   ```

### **Unhappy Path: Invalid Page Size (Out of Bounds)**

1. **HTTP Request:** Client calls `GET /api/customers?page=0&size=101` (exceeds max 100)
2. **Spring Pageable Validation:** Spring's `@PageableDefault` or controller-level constraint validation occurs
3. **Error Handling:** `ConstraintViolationException` caught by global advice
4. **Response:** `400 Bad Request` with validation error in `ProblemDetail.errors` array:
   ```json
   {
     "status": 400,
     "errorCode": "CONSTRAINT_VIOLATION",
     "detail": "Validation failed",
     "errors": [
       { "field": "size", "message": "must be less than or equal to 100" }
     ]
   }
   ```

### **Unhappy Path: Phone Format Invalid**

1. **HTTP Request:** Client calls `GET /api/customers?phone=123&page=0&size=10`
2. **Specification Building:** specification-arg-resolver builds `CustomerSpec` and validates phone param
3. **Validation:** Phone validation constraint (`@Pattern(regexp = "^\\d{4,11}$")` on specification or parameter)
   fails (only 3 digits)
4. **Error Handling:** `ConstraintViolationException` caught by global advice
5. **Response:** `400 Bad Request` with phone format validation error:
   ```json
   {
     "status": 400,
     "errorCode": "CONSTRAINT_VIOLATION",
     "errors": [
       { "field": "phone", "message": "Phone must be 4–11 digits" }
     ]
   }
   ```

### **Edge Case: Empty Result Set (No Matches)**

1. **HTTP Request:** Client calls `GET /api/customers?phone=9999`
2. **Repository Query:** No customers with "9999" in phone
3. **Domain Result:** Repository returns `Page<Customer>` with `content = []`, `totalElements = 0`, `totalPages = 0`
4. **Response:** `200 OK` with empty content array and metadata

### **Edge Case: Page Beyond Available Pages**

1. **HTTP Request:** Client calls `GET /api/customers?page=10&size=10` (only 3 pages exist for 25 customers)
2. **Repository Query:** Spring Data JPA allows querying beyond bounds; returns empty page
3. **Response:** `200 OK` with empty content array, but metadata reflects true totals: `totalElements: 25`,
   `totalPages: 3`

---

## 6. Non-Functional Architecture Decisions

* **Security & Auth:**
    - No authentication enhancements required; project-wide open API policy applies
    - All error responses include `correlationId` (Set by `CorrelationIdFilter` per architecture pattern)
    - `errorCode` set to `CONSTRAINT_VIOLATION` or `INVALID_REQUEST_PARAMETER` per `ErrorCodes` enum
    - CORS headers applied per existing `CorsConfig` global configuration

* **Scale & Performance:**
    - **Database Indexing:** Assume `phone` column has B-tree index from US-CL-001; lastName and firstName columns
      should have compound index for sort efficiency
    - **Pagination Query:** Use database-level LIMIT/OFFSET (Spring Data `Pageable` handles this automatically)
    - **No Caching:** Initial design assumes no caching; future CUST-003 (sortable list) may introduce Spring Cache if
      needed
    - **Expected Response Time:** ~50–100ms for typical page (10 records) with indexed lastName sort and phone filter
    - **Max Page Size: 100** — prevents abuse and ensures consistent performance under load
    - **Sorting:** Always apply lastName + firstName sort to ensure consistent pagination across requests (critical for
      user experience when browsing multiple pages)

* **Backward Compatibility:**
    - Existing phone search endpoint (`GET /api/customers?phone=1234`) continues to work with new paginated response
    - Clients not providing `page`/`size` params receive first page (default page=0, size=10)
    - Response DTO is new (`CustomerPageResponse`), so existing clients expecting a simple list must update

* **Implementation Strategy:**
    - **Specification-Arg-Resolver Pattern:** Follow `TransactionRepositoryAdapter#findTransactionHistory()` design
        - Create `CustomerSpec` interface with `@And` and `@Spec` annotations for phone filtering
        - Use `SpecificationBuilder.specification(CustomerSpec.class)` in adapter to dynamically build specifications
        - Controller method accepts `Specification<CustomerJpaEntity>` parameter (auto-populated by Spring annotation
          processing)
        - Controller method accepts `Pageable` with default sort order
    - **Single Endpoint:** Maintain `GET /api/customers` with conditional filter logic (phone param optional)
    - **Benefits Over Manual Filtering:**
        - Declarative specification definitions (more maintainable, less boilerplate)
        - Reusable specifications across multiple queries
        - Consistent with existing finance module patterns
        - Type-safe filter composition via `SpecificationBuilder`
        - Automatic validation of filter parameters
