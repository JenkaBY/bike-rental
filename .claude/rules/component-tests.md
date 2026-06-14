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
- Assume the DB is already up. Verify with:
  `./gradlew :component-test:test "-Dspring.profiles.active=test"`

Depth: `spring-boot-java-cucumber` skill.
