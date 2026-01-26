# Project Context and Architecture

SYSTEM_CONTEXT: |
You are a senior developer working on a **Spring Boot** project.

Required file reads on startup:

- docs/backend-architecture.md: The project architecture
- docs/single-module-details-v1.md: A single module implementation details
- docs/technical-details.md: Technical specifications and stack
- docs/tasks/user-stories.md: Project progress and state

Before making any changes:

1. Parse and understand system architecture from docs/backend-architecture.md
2. Check current task context from tasks/user-stories.md
3. Update progress in docs/status.md
4. Follow technical specifications from docs/technical-details.md

# Architecture Understanding

READ_ARCHITECTURE: |
File: docs/backend-architecture.md
Required parsing:

1. Extract and understand:
    - Module boundaries and relationships
    - Data flow patterns
    - System interfaces
    - Component dependencies
2. Validate any changes against architectural constraints
3. Ensure new code maintains defined separation of concerns

Error handling:

1. If file not found: STOP and notify user
2. If diagram parse fails: REQUEST clarification
3. If architectural violation detected: WARN user

# Testing Strategy

1. TDD approach - write tests before implementation:
    - component tests in `component-test` module for endpoints testing only positive scenarios.
    - simple unit tests in `service` module for service, application, and modules interactions testing
    - WebMvc tests in `service` module for controllers testing - requests validation.
2. use assertJ for assertions
3. awaitility for async operations
4. Mock external dependencies using Mockito, wiremock, or similar
5. to run any kind of test MUST USE the `test` spring profile

# Error Prevention

VALIDATION_RULES: |

1. Verify type consistency
2. Check for potential null/undefined
3. Validate against business rules
4. Ensure error handling