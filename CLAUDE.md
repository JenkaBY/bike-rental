# CLAUDE.md

@AGENTS.md

---

## Development Methodology: Specification-Driven Development (SDD)

This project uses the **spec-workflow** SDD toolkit (see [README-spec-flow.MD](README-spec-flow.MD)). Requirements,
system designs, and implementation tasks are written as documents under `requirements/` **before any code is touched**.
Each step reads the outputs of the previous step.

### Workflow commands (run in order, per feature)

| Step | Command                                       | Reads                                     | Writes                                  |
|------|-----------------------------------------------|-------------------------------------------|-----------------------------------------|
| 1    | `/spec-requirements <feature description>`    | `architecture.md`, `overview.md`          | `requirements/[id]/[fr]/fr.md`          |
| 2    | `/spec-design requirements/[id]/`             | `fr.md`, `architecture.md`, `overview.md` | `design.md` per FR                      |
| 3    | `/spec-tasks requirements/[id]/[fr]/`         | `fr.md`, `design.md`, source code         | `task-*.md` + `checklist.md`            |
| 4    | `/spec-implement requirements/[id]/[fr]/checklist.md` | `checklist.md`, `task-*.md`        | Code changes via the `dev` subagent     |

Repository-enrichment commands (run once / on major changes): `/project-architecture ./` regenerates the root
`architecture.md`; `/component-overview <path>` regenerates a component's `overview.md`. `/update-docs` syncs task
documents after ad-hoc changes.

### Supporting assets

- **Templates:** `.spec-workflow/` (`fr_template.md`, `task_file_template.md`, `checklist_file_template.md`)
- **Rules:** `.claude/rules/` — short path-scoped hard constraints, auto-loaded when matching files are touched
  (`java-style`, `unit-tests`, `component-tests`, `liquibase`, `workflows`). They are the single source of truth for
  those constraints; skills provide the long-form depth behind them.
- **Subagent:** `.claude/agents/dev.md` — literal-minded task executor used by `/spec-implement`
- **Skills:** `.claude/skills/` — project coding standards (Liquibase, MapStruct, Spring Modulith, DDD persistence,
  Cucumber component tests, WebMvc controller tests, Java/Spring Boot/JUnit best practices). Consult the relevant
  skill before writing code in its area.
- **Specs/output:** `requirements/[REQ_ID]/[fr_index]/` — fr.md, design.md, task files, checklist

## Obsolete / legacy locations (do not use, do not read the legacy directories unless you are asked directly)

- `memory-bank/` — legacy Cline-style "Memory Bank" approach, superseded by SDD documents under `requirements/` and
  this file. Kept for historical reference only; see `memory-bank/OBSOLETE.md`.
- `.github/agents/`, `.github/prompts/`, `.github/instructions/`, `.github/skills/`,
  `.github/copilot-instructions.md` — legacy GitHub Copilot configuration, migrated to `.claude/` (see
  `docs/claude-migration.md`).
- `.cursor/` and `docs/ai-rules/` — legacy Cursor rules and role prompts, superseded by this file and `.claude/`.
