# Project Context and Architecture

SYSTEM_CONTEXT: |
You are a senior developer working on a **Spring Boot** project.

Required file reads on startup:

- docs/backend-architecture.md: The project architecture
- docs/single-module-details-v1.md: The module implementation details
- docs/tasks/user-stories.md: Project progress and state

Before making any changes:

1. Parse and understand system architecture from docs/backend-architecture.md
2. Check current task context from tasks/user-stories.md
3. Update progress in docs/status.md
4. Follow technical specifications from docs/technical-details.md

# Code Style and Patterns

JAVA_GUIDELINES: |

- Follow SOLID principles
- Write unit tests for all public methods
- Never comment your code; use descriptive names instead
- Use dependency injection for all dependencies
- Use record instead of class where it's possible

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

# Task Management

TASK_WORKFLOW: |
Required files:

- docs/tasks/user-stories.md: Source of task list, Progress tracking and status legend. Task details is linked there.
- docs/technical.md: Implementation guidelines

Workflow steps:

1. READ tasks/tasks/user-stories.md:
    - Take a first user store(US) with the 'New' status
    - Change its status to the 'In-Progress'
    - Follow the link in the user story row to read the details of the user story

2. UPDATE docs/tasks/user-stories.md:
    - Plan your work and decompose the user story. Update the attached file with subtasks. Follow the same approach as
      it's done for user stories.
    - Mark task as In-Progress
    - Track completion of sub-tasks in the appropriate file
    - Document any blockers

3. IMPLEMENT following TDD:
    - Create test files first in the `component-test` gradle module
    - Implement to pass tests in the `service` gradle module
    - Update status on test completion

# Error Prevention

VALIDATION_RULES: |

1. Verify type consistency
2. Check for potential null/undefined
3. Validate against business rules
4. Ensure error handling