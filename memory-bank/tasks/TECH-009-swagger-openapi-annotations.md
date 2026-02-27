# [TECH-009] - Swagger / OpenAPI Annotations for All Controllers

**Status:** Pending  
**Added:** 2026-02-27  
**Updated:** 2026-02-27

## Original Request

Update all REST controllers and add Swagger / OpenAPI annotations to document the API.

## Thought Process

The project already has `springdoc-openapi` on the classpath (`springdoc.swagger-ui.enabled: true` in
`application.yaml`) and `springdoc-openapi-starter-webmvc-ui` in `build.gradle`, so no new dependencies are needed.

Currently none of the 14 controllers carry OpenAPI annotations — Swagger UI shows only auto-generated metadata with no
descriptions, response schemas, or error codes. The goal is to add full `springdoc` / `io.swagger.v3.oas.annotations`
annotations to every controller and its DTOs.

### Controllers inventory (14 total across 5 modules)

| Module    | Controller                         | Endpoints                                                            |
|-----------|------------------------------------|----------------------------------------------------------------------|
| customer  | `CustomerQueryController`          | GET /api/customers                                                   |
| customer  | `CustomerCommandController`        | POST /api/customers, PUT /api/customers/{id}                         |
| equipment | `EquipmentQueryController`         | GET /api/equipments, GET /api/equipments/{id}                        |
| equipment | `EquipmentCommandController`       | POST /api/equipments, PUT /api/equipments/{id}                       |
| equipment | `EquipmentTypeQueryController`     | GET /api/equipment-types                                             |
| equipment | `EquipmentTypeCommandController`   | POST /api/equipment-types, PUT /api/equipment-types/{id}             |
| equipment | `EquipmentStatusQueryController`   | GET /api/equipment-statuses                                          |
| equipment | `EquipmentStatusCommandController` | POST /api/equipment-statuses, PUT /api/equipment-statuses/{id}       |
| finance   | `PaymentQueryController`           | GET /api/payments/{id}, GET /api/payments?rentalId                   |
| finance   | `PaymentCommandController`         | POST /api/payments                                                   |
| rental    | `RentalQueryController`            | GET /api/rentals                                                     |
| rental    | `RentalCommandController`          | POST /api/rentals, PATCH /api/rentals/{id}, POST /api/rentals/return |
| tariff    | `TariffQueryController`            | GET /api/tariffs, GET /api/tariffs/{id}, GET /api/tariffs/selection  |
| tariff    | `TariffCommandController`          | POST /api/tariffs, PUT /api/tariffs/{id}                             |

### Annotation strategy

**Controller level:**

- `@Tag(name = "...", description = "...")` — group endpoints in Swagger UI by module/resource

**Endpoint level:**

- `@Operation(summary = "...", description = "...")` — describe each endpoint
- `@ApiResponses({ @ApiResponse(...) })` — document all returned HTTP status codes including error cases
  (400, 404, 409, 422, 500)

**DTO level:**

- `@Schema(description = "...")` on request/response classes
- `@Schema(description = "...", example = "...")` on fields

**Error responses:**

- Document `ProblemDetail` responses for all `@ExceptionHandler` cases visible in `*RestControllerAdvice` classes

**OpenAPI global config:**

- Add `OpenApiConfig` `@Configuration` class with `@Bean OpenAPI` defining info (title, version, description,
  contact) — one place for global metadata

## Implementation Plan

- [ ] 1.1 Add `OpenApiConfig` configuration class with global API info (`@OpenAPIDefinition` or `OpenAPI` bean)
- [ ] 1.2 Annotate **customer** module controllers and DTOs
    - `CustomerQueryController` — GET /api/customers
    - `CustomerCommandController` — POST /api/customers, PUT /api/customers/{id}
    - Request/response DTOs: `CustomerSearchResponse`, `CreateCustomerRequest`, `UpdateCustomerRequest`,
      `CustomerResponse`
- [ ] 1.3 Annotate **equipment** module controllers and DTOs
    - `EquipmentQueryController`, `EquipmentCommandController`
    - `EquipmentTypeQueryController`, `EquipmentTypeCommandController`
    - `EquipmentStatusQueryController`, `EquipmentStatusCommandController`
    - All related request/response DTOs
- [ ] 1.4 Annotate **finance** module controllers and DTOs
    - `PaymentQueryController`, `PaymentCommandController`
    - DTOs: `RecordPaymentRequest`, `RecordPaymentResponse`, `PaymentResponse`
- [ ] 1.5 Annotate **rental** module controllers and DTOs
    - `RentalQueryController`, `RentalCommandController`
    - DTOs: `CreateRentalRequest`, `RentalResponse`, `RentalSummaryResponse`, `RentalReturnResponse`
- [ ] 1.6 Annotate **tariff** module controllers and DTOs
    - `TariffQueryController`, `TariffCommandController`
    - DTOs: `TariffRequest`, `TariffResponse`, `TariffSelectionResponse`
- [ ] 1.7 Document error responses from all `*RestControllerAdvice` classes using `@ApiResponse` on controllers
- [ ] 1.8 Verify Swagger UI renders correctly at `/swagger-ui/index.html` — all groups, schemas, examples visible

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                                            | Status      | Updated    | Notes                                           |
|-----|--------------------------------------------------------|-------------|------------|-------------------------------------------------|
| 1.1 | Add global `OpenApiConfig` class                       | Not Started | 2026-02-27 | Title, version, description, contact            |
| 1.2 | Annotate customer module (2 controllers + DTOs)        | Not Started | 2026-02-27 | `@Tag`, `@Operation`, `@ApiResponse`, `@Schema` |
| 1.3 | Annotate equipment module (6 controllers + DTOs)       | Not Started | 2026-02-27 | Largest module — 6 controllers                  |
| 1.4 | Annotate finance module (2 controllers + DTOs)         | Not Started | 2026-02-27 |                                                 |
| 1.5 | Annotate rental module (2 controllers + DTOs)          | Not Started | 2026-02-27 | Includes PATCH JSON Patch endpoint              |
| 1.6 | Annotate tariff module (2 controllers + DTOs)          | Not Started | 2026-02-27 |                                                 |
| 1.7 | Document error responses from ControllerAdvice classes | Not Started | 2026-02-27 | 400, 404, 409, 422 per module                   |
| 1.8 | Verify Swagger UI end-to-end                           | Not Started | 2026-02-27 | `/swagger-ui/index.html`                        |

## Progress Log

### 2026-02-27

- Task created.
- Inventoried all 14 controllers across 5 modules (customer, equipment, finance, rental, tariff).
- Confirmed `springdoc-openapi-starter-webmvc-ui` is already on the classpath — no new dependencies needed.
- Confirmed `springdoc.swagger-ui.enabled: true` and `springdoc.api-docs.enabled: true` in `application.yaml`.
- Defined annotation strategy: `@Tag` on controllers, `@Operation` + `@ApiResponses` on methods, `@Schema` on DTOs.
- Planned `OpenApiConfig` global configuration bean for title/version/contact.
- Error response documentation scope derived from existing `*RestControllerAdvice` classes per module.

