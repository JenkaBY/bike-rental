# System Design:CUST-001 - Retrieve customer by ID

## 1. Architectural Overview

This story adds a read-only API surface to retrieve a full customer profile by UUID. The change introduces a single,
idempotent query endpoint on the existing customer API that invokes the existing query use-case to obtain domain data
and maps it to the public response DTO. No persistent model or storage changes are required; the flow remains
synchronous request→query→response.

## 2. Impacted Components

* **`CustomerQueryController` (API):**
  **Add a new route `GET /api/customers/{id}` to accept a UUID path parameter, validate its format, and route requests
  to the query use-case. Handle success (200) and error cases (400 invalid UUID, 404 not found) using existing
  problem-detail semantics.**
* **`CustomerQueryUseCase` (Application Use Case):**
  **No change to signature — call `findById(UUID)`; ensure the controller correctly interprets an empty Optional as 404.
  **
* **`CustomerQueryMapper` (Mapper):**
  **Provide or expose a mapping method from domain `CustomerInfo` to the `CustomerResponse` DTO.**
* **`Global Error Handling` (Shared):**
  **Leverage existing `ResourceNotFoundException` semantics and the global ProblemDetail advice to produce 404 responses
  with `errorCode` and `correlationId`.**

## 3. Abstract Data Schema Changes

* **Entity:** None — no schema or persistent model changes required.
* **Relations:** None.

## 4. Component Contracts & Payloads

* **Interaction: `External Client` -> `CustomerQueryController`**
    * **Protocol:** REST
    * **Payload Changes:**
      **Request:** Path param `{id}` as UUID.
      **Response (200):** `CustomerResponse` containing fields: `id` (UUID), `phone`, `firstName`, `lastName`, `email`,
      `birthDate`, `comments`.
      **Error (400):** ProblemDetail indicating path variable parsing/validation failure.
      **Error (404):** ProblemDetail with `errorCode = shared.resource.not_found` and `correlationId`.

* **Interaction: `CustomerQueryController` -> `CustomerQueryUseCase`**
    * **Protocol:** In-process method call
    * **Payload Changes:** Controller passes parsed `UUID id`; receives `Optional<CustomerInfo>`.

* **Interaction: `CustomerQueryUseCase` -> `CustomerQueryMapper`**
    * **Protocol:** In-process method call
    * **Payload Changes:** Map `CustomerInfo` -> `CustomerResponse` for 200 payload.

## 5. Updated Interaction Sequence

1. `External Client` calls `GET /api/customers/{id}` with `{id}` as a string.
2. `CustomerQueryController` validates the path variable and attempts to parse it as UUID.
    - Unhappy path: If parsing fails, controller returns 400 with ProblemDetail describing validation error.
3. On successful parse, `CustomerQueryController` invokes `CustomerQueryUseCase.findById(UUID)`.
4. `CustomerQueryUseCase` returns `CustomerInfo` if found, or an empty result if absent.
    - Unhappy path: If absent, `CustomerQueryController` throws or maps to `ResourceNotFoundException`; the global error
      handler returns 404 with `errorCode` and `correlationId`.
5. If present, `CustomerQueryController` uses `CustomerQueryMapper` to convert `CustomerInfo` -> `CustomerResponse`.
6. `CustomerQueryController` returns 200 with `CustomerResponse` body.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** No changes — endpoints remain unauthenticated per current system-wide configuration. Caller must
  be aware of public visibility.
* **Scale & Performance:** Read is simple and low-cost; no additional caching required. If high read volume appears
  later, introduce a read-side cache in front of the query use-case or use a short-lived in-memory cache at the API
  layer.
* **Observability:** Ensure controller requests include existing correlation id in logs; error responses must include
  `correlationId` as produced by the global advice.
* **Validation & Error Semantics:** Follow existing controller-level validation conventions: invalid UUID -> 400
  ProblemDetail; missing resource -> 404 ProblemDetail with `shared.resource.not_found` `errorCode`.
