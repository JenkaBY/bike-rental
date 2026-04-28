# User Story: CUST-002 — Paginated Customer List with Optional Phone Filter

## 1. Description

**As a** System operator / admin user  
**I want to** retrieve a paginated list of customers, optionally filtered by phone number  
**So that** I can browse and manage customer records efficiently without requiring a phone search

---

## 2. Context & Business Rules

* **Trigger:** Operator loads customer list/management screen; admin requests customer overview without phone knowledge
* **Existing Behavior:** `GET /api/customers?phone={digits}` returns filtered list (not paginated)
* **New Behavior:** `GET /api/customers` (no params) returns all customers paginated; phone param remains optional
* **Rules Enforced:**
    - `phone` parameter is **optional**; when absent, return ALL customers with pagination
    - When `phone` is **provided**, apply filter first, then paginate results
    - Results are **always sorted by lastName ascending** (A–Z), then by firstName if lastName is equal
    - Page index is **zero-based** (page 0 = first page)
    - Default page size: **10 records**; minimum: **1**, maximum: **100**
    - Response always includes pagination metadata: `totalElements`, `totalPages`, `currentPage`, `pageSize`

---

## 3. Non-Functional Requirements (NFRs)

* **Performance:**
    - Page load must complete in < 1 second (typical response ~50–100ms for 10 records)
    - Database query must use indexed lookups where applicable

* **Security/Compliance:**
    - No authentication required (project-wide open API policy)
    - All error responses must include `correlationId` and `errorCode` per project conventions
    - Responses must be RFC 7807 `ProblemDetail` for errors

* **Usability/Other:**
    - Response schema must be backward compatible (new `CustomerPageResponse` DTO wraps customer array)
    - Pagination metadata must follow Spring Data `Page<T>` conventions for frontend frameworks
    - OpenAPI/Swagger annotations must document all query parameters and response schema

---

## 4. Acceptance Criteria (BDD)

### **Scenario 1: List all customers with default pagination (sorted by last name)**

* **Given** 25 customers exist in the system (unsorted order in database)
* **When** a client calls `GET /api/customers?page=0&size=10`
* **Then** the server responds `200 OK` with body containing:
    - First 10 customers sorted by lastName (A–Z), then firstName
    - Example order: Adams John, Bailey Jane, Carter Mike, Davis Sarah, ... (sorted A–Z by last name)
    - `totalElements: 25`, `totalPages: 3`, `currentPage: 0`, `pageSize: 10`, `hasNext: true`, `hasPrevious: false`

### **Scenario 2: List customers without explicit page parameters (use defaults, sorted by last name)**

* **Given** 15 customers exist
* **When** a client calls `GET /api/customers` (no query params)
* **Then** the server responds `200 OK` with:
    - First 10 customers sorted by lastName (A–Z), then firstName
    - `totalElements: 15`, `totalPages: 2`, `currentPage: 0`, `pageSize: 10`
    - Page 2 contains remaining 5 customers, also in last name order

### **Scenario 3: Filter customers by phone number with pagination (results sorted by last name)**

* **Given** 25 total customers; 5 have phone numbers containing "1234"
* **When** a client calls `GET /api/customers?phone=1234&page=0&size=10`
* **Then** the server responds `200 OK` with:
    - All 5 matching customers sorted by lastName (A–Z), then firstName
    - `totalElements: 5`, `totalPages: 1`, `currentPage: 0`, `pageSize: 10`

### **Scenario 4: Paginate through filtered results (maintaining last name sort)**

* **Given** 50 customers; 20 match phone "5678"
* **When** a client calls `GET /api/customers?phone=5678&page=1&size=10`
* **Then** the server responds `200 OK` with:
    - Customers 10–19 of the filtered set, sorted by lastName (A–Z)
    - `totalElements: 20`, `totalPages: 2`, `currentPage: 1`, `pageSize: 10`, `hasNext: false`

### **Scenario 5: Request page beyond available pages**

* **Given** 25 customers exist
* **When** a client calls `GET /api/customers?page=10&size=10` (only 3 pages exist)
* **Then** the server responds `200 OK` with:
    - Empty content array `[]`
    - `totalElements: 25`, `totalPages: 3`, `currentPage: 10`, `pageSize: 10`

### **Scenario 6: Invalid page parameter (non-numeric)**

* **Given** valid request setup
* **When** a client calls `GET /api/customers?page=abc&size=10`
* **Then** the server responds `400 Bad Request` with `ProblemDetail`:
  ```json
  {
    "type": "about:blank",
    "title": "Bad Request",
    "status": 400,
    "detail": "Failed to convert parameter 'page' from type 'String' to required type 'int'",
    "errorCode": "INVALID_REQUEST_PARAMETER",
    "correlationId": "uuid-..."
  }
  ```

### **Scenario 7: Invalid page size (out of bounds)**

* **Given** valid request
* **When** a client calls `GET /api/customers?page=0&size=101` (exceeds max 100)
* **Then** the server responds `400 Bad Request` with validation error indicating size constraint

### **Scenario 8: Invalid page size (negative or zero)**

* **Given** valid request
* **When** a client calls `GET /api/customers?page=0&size=0`
* **Then** the server responds `400 Bad Request` with validation error

### **Scenario 9: Phone filter with empty result set**

* **Given** 25 customers exist; none match "9999"
* **When** a client calls `GET /api/customers?phone=9999`
* **Then** the server responds `200 OK` with:
    - Empty content array `[]`
    - `totalElements: 0`, `totalPages: 0`, `currentPage: 0`, `pageSize: 10`

### **Scenario 10: Phone filter requires correct length (backward compatibility)**

* **Given** phone search feature expects 4–11 digits
* **When** a client calls `GET /api/customers?phone=123` (only 3 digits)
* **Then** the server responds `400 Bad Request` with validation error (if phone is provided, it must be 4–11 digits)

### **Scenario 11: Results are always sorted by lastName (A–Z)**

* **Given** 5 customers in the system:
    - Smith, John
    - Adams, Jane
    - Carter, Mike
    - Bailey, Sarah
    - Davis, Robert
* **When** a client calls `GET /api/customers?page=0&size=10`
* **Then** the server responds `200 OK` with customers in this order:
    1. Adams, Jane
    2. Bailey, Sarah
    3. Carter, Mike
    4. Davis, Robert
    5. Smith, John
* **And** the order remains consistent across all pages and filtered queries

---

## 5. Out of Scope

- **Sorting by Other Fields:** Sorting by phone, registration date, or custom sort order — defer to future CUST-003
  story
- **Advanced Filtering:** Multi-field filters (name AND phone, date range) — defer to future stories
- **Export:** CSV/PDF export of paginated list — separate story
- **Real-time Updates:** WebSocket/Server-Sent Events for live customer count or stream updates
- **Authentication/Authorization:** Project-wide open API; no role-based filtering in this story
- **Domain Model Changes:** This is a read-only query enhancement; no persistence layer modifications
- **Component/Integration Tests:** Covered by follow-up task (CUST-002-component-tests)
- **API Versioning:** Uses existing `/api/customers` endpoint; no v2 created

---

## 6. Relationship to Other Stories

- **Depends on:** None (CUST-002 is independent)
- **Blocks:** CUST-003 (Sortable Customer List), CUST-004 (Customer Filters by Name/DOB)
- **Related:** US-CL-001 (Phone search), CUST-001 (Retrieve by ID)

---

## 7. API Contract

### **Endpoint**

```
GET /api/customers
```

### **Query Parameters**

| Name  | Type    | Required | Default | Validation              | Description                    |
|-------|---------|----------|---------|-------------------------|--------------------------------|
| phone | string  | NO       | —       | 4–11 digits if provided | Phone digits to filter results |
| page  | integer | NO       | 0       | >= 0                    | Zero-indexed page number       |
| size  | integer | NO       | 10      | 1–100                   | Records per page               |

### **Response: 200 OK**

```json
{
  "content": [
    {
      "id": "uuid",
      "phone": "string",
      "firstName": "string",
      "lastName": "string",
      "email": "string|null",
      "birthDate": "date|null",
      "comments": "string|null",
      "createdAt": "instant"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "currentPage": 0,
  "pageSize": 10,
  "hasNext": true,
  "hasPrevious": false
}
```

### **Responses: Error Cases**

- `400 Bad Request` — Invalid page/size/phone format
- `500 Internal Server Error` — Unhandled exception

All error responses include `errorCode` and `correlationId`.









