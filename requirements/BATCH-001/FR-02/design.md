# System Design: FR-02 — Batch Customer Fetch Endpoint

## 1. Architectural Overview

FR-02 introduces a new batch-lookup read path through all four layers of the `customer` module. Unlike the equipment
module — which already has an internal batch-use-case used by the rental module — the customer module has no existing
multi-ID lookup capability. A new use-case interface, service, and repository method must be added at each layer.

The new HTTP handler at `GET /api/customers/batch` is added to the existing `CustomerQueryController`. Input
validation (required parameter, UUID format, 100-item cap) and de-duplication are enforced at the web boundary.
The application layer delegates to a new `GetCustomersByIdsUseCase` whose implementation calls a new
`CustomerRepository.findByIds()` domain port. The response is a flat JSON array of full `CustomerResponse` objects,
matching the schema of `GET /api/customers/{id}`. A new list-mapping method is added to `CustomerWebMapper`.

## 2. Impacted Components

* **`CustomerQueryController` (API):** Must gain a new handler method bound to `GET /api/customers/batch`. The
  method must accept `ids` as a required comma-separated query parameter, validate that all values are well-formed
  UUIDs, reject lists larger than 100 items with `400 Bad Request`, silently de-duplicate the list, delegate to
  `GetCustomersByIdsUseCase`, and map the result through `CustomerWebMapper` to a `List<CustomerResponse>`. OpenAPI
  annotations must document the endpoint summary, `ids` parameter format and example, and response codes `200`
  and `400`.

* **`CustomerWebMapper` (Web Mapper):** Must gain a new mapping method that converts a `List<Customer>` to a
  `List<CustomerResponse>`. Each element is mapped using the already-defined `toResponse(Customer)` method.

* **`GetCustomersByIdsUseCase` (Application Use-Case Contract) — NEW:** A new interface in the application layer
  exposing `List<Customer> execute(Collection<UUID> ids)`. The controller depends on this interface, not on the
  implementation.

* **`GetCustomersByIdsService` (Application Service) — NEW:** Implements `GetCustomersByIdsUseCase`. Delegates
  directly to `CustomerRepository.findByIds()`. Contains no business logic beyond the delegation.

* **`CustomerRepository` (Domain Port):** Must gain a new method `findByIds(Collection<CustomerRef> ids)` that returns
  all customer records matching the provided UUIDs. Missing IDs are naturally absent from the returned list.

* **`CustomerRepositoryAdapter` (Infrastructure Persistence Adapter):** Must implement the new `findByIds` port
  method. Must issue a single `IN`-predicate query against the `customers` table keyed on the UUID primary-key
  column, returning all matching rows.

## 3. Abstract Data Schema Changes

None. The `customers` table and all existing column structures remain unchanged. The new repository method queries
the existing UUID primary-key column with an `IN` predicate.

## 4. Component Contracts & Payloads

* **Interaction: External Client → `CustomerQueryController`**
    * **Protocol:** HTTP GET
    * **Payload Changes:** New required query parameter `ids` — a comma-separated list of RFC 4122 UUIDs
      (e.g., `ids=3fa85f64-5717-4562-b3fc-2c963f66afa6,9cb12d11-0000-0000-0000-000000000002`). Maximum 100 values.
      Existing endpoints and parameters are unaffected.

* **Interaction: `CustomerQueryController` → `GetCustomersByIdsUseCase`**
    * **Protocol:** In-process method call
    * **Payload Changes:** New use-case method `execute(List<UUID> ids)` receiving a de-duplicated, validated list
      of UUIDs. Returns `List<Customer>` (domain objects).

* **Interaction: `GetCustomersByIdsService` → `CustomerRepository`**
    * **Protocol:** In-process method call (domain port)
    * **Payload Changes:** New port method `findByIds(Collection<CustomerRef> ids)` receiving the UUID collection.
      Returns `List<Customer>`. Missing UUIDs are absent from the result without error.

* **Interaction: `CustomerQueryController` → External Client**
    * **Protocol:** HTTP response
    * **Payload Changes:** New response shape: a flat JSON array of `CustomerResponse` objects. Each object is
      identical in structure to the body returned by `GET /api/customers/{id}`. No pagination envelope.

## 5. Updated Interaction Sequence

**Happy path — all requested UUIDs exist:**

1. External client sends `GET /api/customers/batch?ids=uuid-1,uuid-2,uuid-3`.
2. `CustomerQueryController` parses `ids` into `[uuid-1, uuid-2, uuid-3]`, validates each value is a well-formed
   UUID, confirms list size ≤ 100, de-duplicates (no-op here), and produces a clean `List<UUID>`.
3. `CustomerQueryController` calls `getCustomersByIdsUseCase.execute([uuid-1, uuid-2, uuid-3])`.
4. `GetCustomersByIdsService` calls `CustomerRepository.findByIds([uuid-1, uuid-2, uuid-3])`.
5. `CustomerRepositoryAdapter` issues a single `SELECT … WHERE id IN (uuid-1, uuid-2, uuid-3)` query and returns
   `List<Customer>` with 3 elements.
6. `CustomerQueryController` calls `mapper.toResponses(customerList)`.
7. `CustomerWebMapper` maps each `Customer` to `CustomerResponse`.
8. `CustomerQueryController` returns `200 OK` with a JSON array of 3 objects.

**Happy path — some UUIDs do not exist:**

1. External client sends `GET /api/customers/batch?ids=uuid-1,uuid-2,uuid-unknown`.
2. Steps 2–5 proceed as above; the repository returns only the 2 matching records.
3. Steps 6–8 proceed normally; the response array contains 2 objects. No error is raised for the missing UUID.

**Happy path — empty result:**

1. External client sends `GET /api/customers/batch?ids=uuid-none`.
2. Steps 2–5 proceed as above; the repository returns an empty list.
3. `CustomerQueryController` returns `200 OK` with an empty JSON array `[]`.

**Unhappy path — `ids` parameter absent:**

1. External client sends `GET /api/customers/batch` without `ids`.
2. `CustomerQueryController` rejects the request with `400 Bad Request`.

**Unhappy path — malformed UUID in `ids`:**

1. External client sends `GET /api/customers/batch?ids=not-a-valid-uuid`.
2. `CustomerQueryController` detects the malformed value during UUID parsing and returns `400 Bad Request`.

**Unhappy path — list exceeds 100 items:**

1. External client sends `GET /api/customers/batch?ids=<101 UUIDs>`.
2. `CustomerQueryController` detects that the list size exceeds the cap and returns `400 Bad Request`.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** The `ids` values are parsed into a strongly-typed `UUID` collection before reaching the
  persistence layer. UUID parsing rejects any non-conforming input at the web boundary. No string interpolation into
  SQL occurs; the `IN`-predicate query uses parameterised bindings, eliminating SQL injection risk.
* **Scale & Performance:** All matching records are resolved in a single database round-trip. No N+1 queries are
  introduced. The 100-item cap bounds the `IN` clause size and protects against oversized queries under high
  cardinality.
