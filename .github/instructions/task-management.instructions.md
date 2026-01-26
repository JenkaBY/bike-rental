---
description: 'Guidelines for managing tasks and issues in software projects.'
applyTo: 'docs/tasks/**.md'
---

# Task Management Guidelines

This instruction file defines the workflow for managing tasks and user stories in the project, ensuring consistent
implementation practices and proper tracking of progress.

## Required Files

Before starting any task, ensure you have access to these essential files:

- **docs/tasks/user-stories.md**: Source of task list, progress tracking, and status legend with links to detailed user
  story files
- **docs/technical-details.md**: Implementation guidelines and technical specifications
- **docs/backend-architecture.md**: System architecture and component relationships
- **docs/single-module-details-v1.md**: Package structure, naming conventions, and coding standards reference

## Task Workflow

Follow this structured workflow when working on tasks. The workflow ensures consistent progress tracking and adherence
to project standards.

### Step 1: Identify and Select Task

1. **Read** `docs/tasks/user-stories.md` to identify the current task
2. **Look for** a User Story (US) with 'In-Progress' status:
    - If found, follow the link to the US details file
    - Check whether the US has been decomposed into subtasks
    - If not decomposed, proceed to Step 2 to plan and decompose
    - If decomposed, proceed to Step 3 to work on subtasks
3. **If no 'In-Progress' US found**:
    - Take the first 'New' US from the list
    - Change its status to 'In-Progress' in `user-stories.md`
    - Follow the link to read the detailed user story
    - Proceed to Step 2 for decomposition

### Step 2: Plan and Decompose User Story

When working with a user story that hasn't been decomposed:

1. **Analyze** the user story requirements and acceptance criteria
2. **Plan** your implementation approach considering:
    - System architecture from `docs/backend-architecture.md`
    - Technical constraints from `docs/technical-details.md`
    - Module structure from `docs/single-module-details-v1.md`
3. **Decompose** the user story into logical subtasks
4. **Document** subtasks in the US details file following the format used in other 'In-Progress' user stories
5. **Update** `docs/tasks/user-stories.md` with the task status
6. Proceed to Step 3

### Step 3: Implement Using TDD

Follow Test-Driven Development (TDD) principles for all implementation work:

#### Component Tests (First)

1. **Create** test files in the `component-test` Gradle module
2. **Write** tests covering positive scenarios for endpoints
3. **Ensure** tests are comprehensive and follow project conventions

#### Implementation (Second)

1. **Implement** code in the `service` Gradle module to pass the tests
2. **Follow** the architecture patterns defined in project documentation
3. **Adhere** to package structure and naming conventions

#### Additional Testing

1. **Write** unit tests in the `service` module for:
    - Service layer logic
    - Application layer interactions
    - Module interactions
2. **Create** WebMvc tests in the `service` module for:
    - Controller endpoint testing
    - Request validation
3. **Use** AssertJ for assertions
4. **Use** Awaitility for async operations testing
5. **Mock** external dependencies using Mockito, WireMock, or similar tools

### Step 4: Track Progress and Update Status

As you complete subtasks:

1. **Take** the first 'In-Progress' subtask and validate its implementation
2. **If incomplete**, continue working on it
3. **If complete**, mark the subtask as 'Done' in the US details file
4. **If no 'In-Progress' subtasks exist**, take the next 'New' subtask:
    - Change its status to 'In-Progress'
    - Begin implementation following Step 3
5. **When all subtasks are complete**:
    - Mark the entire User Story as 'Done' in `docs/tasks/user-stories.md`
    - Stop working on this US

### Step 5: Validate and Ensure Compliance

Before marking any subtask or user story as complete:

1. **Verify** code adheres to project architecture defined in `docs/backend-architecture.md`
2. **Validate** package structure follows the guidelines in `docs/single-module-details-v1.md`
3. **Ensure** naming conventions are consistent with project standards
4. **Check** coding standards compliance
5. **Confirm** all tests pass (component tests, unit tests, WebMvc tests)
6. **Document** any blockers or issues encountered

## Status Values

Use these consistent status values across all task tracking:

| Status          | Meaning                   | When to Use                          |
|-----------------|---------------------------|--------------------------------------|
| **New**         | Not started               | Initial state for new tasks          |
| **In-Progress** | Currently being worked on | Active development                   |
| **Done**        | Completed and validated   | All acceptance criteria met          |
| **Blocked**     | Cannot proceed            | Waiting on dependencies or decisions |

## Best Practices

1. **Always read required files** before starting any task
2. **Update status immediately** when changing task states
3. **Document blockers** as soon as they are identified
4. **Write tests first** following TDD methodology
5. **Validate compliance** before marking tasks complete
6. **Keep user story files updated** with current progress
7. **Follow architectural patterns** consistently across the codebase
8. **Maintain clear communication** through status updates and documentation

## Common Workflow Patterns

### Starting a New Sprint

1. Review `docs/tasks/user-stories.md` for prioritized tasks
2. Select the highest priority 'New' user story
3. Follow the workflow from Step 1

### Resuming Work After Break

1. Check `docs/tasks/user-stories.md` for 'In-Progress' items
2. Review the US details file for current subtask status
3. Continue from the last 'In-Progress' subtask

### Handling Blockers

1. Document the blocker in the US details file
2. Change subtask status to 'Blocked'
3. Update `docs/tasks/user-stories.md` if the entire US is blocked
4. Move to another available task if possible

## References

- [Backend Architecture](docs/backend-architecture.md) - System design and component relationships
- [Technical Details](docs/technical-details.md) - Technical specifications and stack
- [Single Module Details](docs/single-module-details-v1.md) - Code structure and conventions
- [User Stories](docs/tasks/user-stories.md) - Current task list and progress
