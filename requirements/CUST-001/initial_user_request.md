# Initial user request — Retrieve customer by ID

Date: 2026-04-16
Requester: (requested via chat)

Original user request:

> I need a new feature to retrieve customer by id

Purpose / summary:

Create a new behaviour in the Customer bounded context to allow API clients to fetch a full customer profile by UUID.
The feature will expose a read (query) HTTP endpoint under the existing `/api/customers` surface that returns the full
`CustomerResponse` DTO for a given customer UUID.

Why (business justification):

- Several UI workflows and downstream processes require looking up a customer profile by their UUID during rental
  creation and customer management flows.
- Providing a dedicated endpoint avoids repeated client-side search calls and simplifies integrations (e.g., component
  tests and frontend code).

Scope (what's in scope for the initial request):

- Add a GET endpoint: `GET /api/customers/{id}` where `{id}` is a UUID.
- The endpoint returns 200 + `CustomerResponse` when the customer is found.
- The endpoint returns 404 + `ProblemDetail` when the customer is not found (use existing `ResourceNotFoundException`
  semantics and error code conventions).
- Basic request/response validation: path variable should be parsed as UUID (invalid format yields 400 as per existing
  controller conventions).
- Use the existing application use case `CustomerQueryUseCase.findById(UUID)` and mapper `CustomerQueryMapper` to
  convert domain data to `CustomerResponse`.

Out of scope (for this change):

- Authentication/authorization (project currently exposes `/api/**` open).
- Changes to persistent model, database migrations, or business logic beyond the read path.
- Component/integration tests (will be proposed in follow-up user stories).

Acceptance criteria (high level):

1. Given an existing customer with UUID `cId`, when a client calls `GET /api/customers/{cId}`, then the server responds
   200 and body contains `CustomerResponse` with the customer's id, phone, firstName, lastName, email, birthDate,
   comments.

2. Given a non-existing UUID, when a client calls `GET /api/customers/{nonExistingId}`, then the server responds 404
   with a `ProblemDetail` body including `errorCode` set to `shared.resource.not_found` and `correlationId` per project
   conventions.

3. Given an invalid UUID string as `{id}` (not a valid UUID format), the server responds 400 with a `ProblemDetail`
   describing the path variable conversion/validation error.

Files / references to inspect when implementing:

- `architecture.md` — overview of module boundaries and exposed endpoints
- `service/src/main/java/com/github/jenkaby/bikerental/customer/application/usecase/CustomerQueryUseCase.java` —
  contains `Optional<CustomerInfo> findById(UUID id);`
- `service/src/main/java/com/github/jenkaby/bikerental/customer/web/query/CustomerQueryController.java` — location to
  add the endpoint (currently has searchByPhone endpoint only)
- `service/src/main/java/com/github/jenkaby/bikerental/customer/web/query/dto/CustomerResponse.java` — response DTO to
  return
- `service/src/main/java/com/github/jenkaby/bikerental/customer/web/query/mapper/CustomerQueryMapper.java` — mapper to
  convert `CustomerInfo` -> `CustomerResponse` (add method if missing)
- `service/src/main/java/com/github/jenkaby/bikerental/shared/exception/ResourceNotFoundException.java` — use for 404
  behaviour

Notes / implementation hints:

- Follow project conventions: controller slice annotated with `@ApiTest` for WebMvc tests, `@Validated` on controllers,
  use `ProblemDetail` for error payload, include `correlationId` and `errorCode` in error responses per global advice.
- The Customer module follows hexagonal boundaries — use `CustomerQueryUseCase.findById(UUID)` rather than accessing
  repositories directly from controller.

Next steps (proposal):

- I will propose a small set of User Stories (INVEST-aligned) that cover implementation, validation tests, and component
  test coverage, and ask for your approval before creating the detailed FR files.

Original chat message is preserved above for traceability.

