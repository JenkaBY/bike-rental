# Task Priorities

This document defines the task priority levels used in the Memory Bank and the tasks index. Use these levels
consistently when creating or updating tasks.

Priority levels

- URGENT
    - Meaning: Must be completed to support the main user flow or unblock dependent work. Fixes or features that, if
      missing, prevent core functionality from working.
    - Typical examples: payment acceptance for rentals, rental creation, critical security fixes that block development
      or release.
    - Behaviour: Mark tasks as URGENT only when they are on the critical path. These tasks should be scheduled
      immediately and given developer time priority.

- MEDIUM
    - Meaning: Important to have; improves functionality or developer experience but the system can operate without it
      temporarily.
    - Typical examples: reporting features, performance improvements that are not blocking, administrative convenience
      features.
    - Behaviour: Schedule in upcoming sprints; deprioritize if URGENT work appears.

- LOW
    - Meaning: Nice-to-have; optional features or enhancements the app can run without.
    - Typical examples: UI polish, extended admin options, additional non-critical validations.
    - Behaviour: Keep a backlog of LOW tasks and revisit after core work is complete.

Guidelines

- Use URGENT sparingly — reserve it for tasks that truly block main flows or produce unacceptable risk.
- When changing a priority, add a short note in the task's progress log explaining why the priority changed and who
  approved it.
- When listing tasks in `memory-bank/tasks/_index.md`, include the priority in the task line (e.g.,
  `- [US-FN-001] Прием оплаты - URGENT, finance module`).
- For planning, surface URGENT tasks at the top of the roadmap and in sprint planning.

Audit

- The PM or tech lead should occasionally review URGENT tasks and downgrade them when the blocking issue is resolved.

---

Last updated: 2026-02-04
