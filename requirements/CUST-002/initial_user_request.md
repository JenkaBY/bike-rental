# Initial User Request — Paginated Customer List with Optional Phone Filter

**Date:** 2026-04-28  
**Requester:** User via AI assistant  
**Status:** New requirement proposal

---

## Original User Request

> Add a task to get paged customers.

---

## Purpose / Summary

Enhance the existing customer query endpoint to support **paginated listing of all customers** while preserving the
ability to **filter by phone number**. This enables operators to browse customer records without requiring a phone
search and provides a foundation for admin dashboards and customer management workflows.

Currently, `GET /api/customers` requires a `phone` query parameter; the enhanced endpoint should:

- Support pagination (page, size, sorting metadata)
- Make the `phone` parameter **optional**
- Return all customers when no `phone` is provided
- Maintain backward compatibility with phone filtering when provided

---

## Why (Business Justification)

1. **Customer Management:** Operators and admins need to view and manage customer records in a list format
2. **UI Dashboards:** Admin dashboards require paginated customer lists for overview screens
3. **Operational Efficiency:** Browsing customers without a known phone number avoids manual search-and-click workflows
4. **Backward Compatibility:** Existing phone search workflows should continue functioning unchanged

---

## Scope (In Scope)

- Modify `GET /api/customers` endpoint to accept **optional** query parameters: `page`, `size`, `phone`
- Return paginated response with metadata: `totalElements`, `totalPages`, `currentPage`, `pageSize`
- Create new response DTO: `CustomerPageResponse` (wraps paginated customer list)
- Default pagination: page size = 10, max size = 100, zero-indexed pages
- Maintain existing phone filtering logic when `phone` param is provided
- Update `CustomerQueryUseCase` and `CustomerRepository` to support pagination
- Add OpenAPI/Swagger documentation for new parameters and response
- WebMvc tests covering both paginated list and filtered scenarios

---

## Out of Scope

- **Sorting by Other Fields:** Custom sort order by phone, registration date, or configurable columns (defer to
  CUST-003)
- **Export:** CSV/PDF export functionality
- **Advanced Filtering:** Multi-field filters (name + phone, date range) beyond phone
- **Real-time Updates:** WebSocket/SSE for live customer count
- **Authentication/Authorization:** Project-wide open API (no changes required)
- **Domain Model Changes:** Read-only query path only, no persistence changes
- **Component Tests:** Will be covered in follow-up task

---

## Acceptance Criteria (High Level)

1. **List all customers (default):** `GET /api/customers?page=0&size=10` returns paginated list of first 10 customers
   with metadata
2. **Filter by phone:** `GET /api/customers?phone=1234&page=0&size=10` returns filtered + paginated results
3. **Pagination validation:** Invalid `page` or `size` values yield 400 + validation error
4. **Backward compatibility:** Phone filter works exactly as before when provided
5. **Response includes:** totalElements, totalPages, currentPage, pageSize, customerList array

---

## Files / References to Inspect

- `architecture.md` — module boundaries, endpoint documentation
- `service/src/main/java/com/github/jenkaby/bikerental/customer/web/query/CustomerQueryController.java` — current
  `searchByPhone()` method
- `service/src/main/java/com/github/jenkaby/bikerental/customer/application/usecase/CustomerQueryUseCase.java` —
  interface to extend
- `service/src/main/java/com/github/jenkaby/bikerental/customer/web/query/dto/` — response DTOs
- `service/src/main/java/com/github/jenkaby/bikerental/customer/domain/repository/CustomerRepository.java` — repository
  interface

---

## Implementation Notes

- Follow Spring Data JPA `Page<T>` and `Pageable` patterns
- Use `@RequestParam(required = false)` for optional `phone` parameter
- Maintain `/api/customers/{id}` path-based lookup (no conflict)
- Validation: `page >= 0`, `1 <= size <= 100`
- Default size = 10 if not provided
- Phone filter applies even when pagination is used
- Use `ProblemDetail` error responses per project conventions

---

## Next Steps

1. ✅ BA review and requirement approval (completed)
2. Create formal FR file (`01/fr.md`) with BDD scenarios
3. Implementation (controller, service, repository, DTOs, mappers)
4. Unit tests for pagination logic
5. WebMvc tests for controller and error cases
6. Component tests (Cucumber) for happy paths


