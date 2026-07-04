---
paths:
  - "component-test/**"
---

# Component Tests (`component-test` module) — hard constraints

- **Happy paths + business-rule failures only.** Request-validation negative cases belong in WebMvc tests in the
  `service` module — never write scenarios for them here.
- Features live in `component-test/src/test/resources/features/{module-name}/`; runner is `RunComponentTests`. Tag
  `@skip` to suppress a scenario; tag `@run` + swap the filter line to run a single scenario during development (revert
  before finishing).
- **AssertJ only**; for `BigDecimal` use `isEqualByComparingTo()` to avoid scale mismatches.
- Scenario state goes through `ScenarioContext` (cleared by hooks); captured messages through `LocalMessagesStore`.
- DataTable/parameter converters live in `transformer/`, named `{Domain}Transformer` — **never convert a DataTable
  inside a step class**; step methods receive already-transformed objects (`@DataTableType` entry transformers).
- **A request with nested fields (a list/collection of sub-objects) is prepared across multiple steps, not one.** A
  sub-part step parses the nested rows (one row per sub-object) and stores them in `ScenarioContext`; a
  `…request is prepared with the following data` step takes a `{Request}Builder` record (in `model/`) of the top-level
  fields and merges the stored sub-part via `builder.toRequest(subPart)`. Reference:
  `the pricing params for tariff request are` + `the tariff v2 request is prepared with the following data`.
- Reusable steps go in `steps/common/`; group scenario variations with Scenario Outline + Examples.
- **Step classes are stateless.** No instance fields besides constructor-injected dependencies; ALL request/response
  state flows through `ScenarioContext` (it also carries `binaryResponse` for non-JSON bodies).
- **No raw JSON in feature files** (no docstring payloads). Every request body is a DataTable converted by a
  `@DataTableType` transformer into the real request record and stored by a `the <domain> request is` Given-step.
- **Success responses are asserted with typed objects**: a response transformer + a `the <domain> response contains`
  Then-step that checks only the non-null expected fields and stores the returned id into
  `ScenarioContext.requestedObjectId`. JsonPath tables (`the response contains` path/value) are reserved for
  ProblemDetail/error assertions only.
- **HTTP-calling steps live ONLY in `steps/common/WebRequestSteps`** — including the binary download step
  `a {httpMethod} request for {string} content has been made to {string} endpoint`. Domain step classes contain only
  domain-specific assertions (e.g. PDF text extraction). HTTP steps must not mutate `ScenarioContext` headers — build
  a local `HttpHeaders` copy when a step needs to override one.
- **Long constant strings (sha256, UUIDs) go through `Aliases`** (e.g. `SHA_ZERO`); transformers resolve them via
  `Aliases.getValueOrDefault`.
- **Every new table goes into `DbSteps.TABLE_TO_TRUNCATE`** (respecting FK order), and JDBC-seeded entities must
  populate every NOT NULL column explicitly (`JpaEntityInserter` inserts all fields — e.g. `version`/`lockVersion` = 0).
- Assume the DB is already up. Verify with:
  `./gradlew :component-test:test "-Dspring.profiles.active=test"`

Depth: `spring-boot-java-cucumber` skill.
