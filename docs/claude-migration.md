# AI Assistant Configuration Migration: Copilot/Cursor → Claude

**Date:** 2026-06-12
**Branch:** `feature/migrate-to-claude`
**Active methodology (confirmed):** **SDD — Specification-Driven Development** via the
[spec-workflow](https://github.com/arturhaikou/spec-workflow/) toolkit (see `README-spec-flow.MD`). Specs live under
`requirements/[REQ_ID]/[fr_index]/` (`fr.md` → `design.md` → `task-*.md` + `checklist.md`) and are written before code.

---

## 1. Inventory (before migration)

| Location | Purpose | Verdict |
|---|---|---|
| `.github/copilot-instructions.md` | Copilot repo instructions — **unfilled template** (placeholders only) | Deprecated, not migrated (no content worth keeping; real conventions live in `AGENTS.md`) |
| `.github/agents/*.agent.md` (business-analyst, architect, team-lead, dev-manager, dev) | SDD workflow agents (Copilot/VS Code format) | **Migrated** |
| `.github/agents/4.1-Beast.agent.md` | Generic "GPT-5mini Beast Mode" persona | Deprecated, not migrated (model-specific, unrelated to SDD; its `memory.instruction.md` convention never materialized) |
| `.github/prompts/project-architecture.prompt.md`, `component-overview.prompt.md` | Repo-enrichment prompts (generate `architecture.md`, `overview.md`) | **Migrated** |
| `.github/prompts/dev-manger-delegator.prompt.md` | Kick-off prompt for dev-manager | Folded into `/spec-implement` |
| `.github/prompts/java-junit.prompt.md`, `component-test.prompt.md` | Testing best-practice prompts | **Migrated** (as skills) |
| `.github/prompts/java-springboot.prompt.md` | Spring Boot best practices | Deprecated, not migrated — duplicate of `springboot.instructions.md` (which was migrated) |
| `.github/prompts/update-docs.prompt.md` | Doc-sync prompt | **Migrated** |
| `.github/instructions/java.instructions.md`, `springboot.instructions.md` | Path-scoped (`applyTo`) coding standards | **Migrated** (as skills) |
| `.github/instructions/github-actions-ci-cd-best-practices.instructions.md` | CI/CD guidance for `.github/workflows/` | **Migrated** (as skill — workflows remain active) |
| `.github/instructions/agent-skills.instructions.md` | Meta-guide for authoring Copilot skills | Deprecated, not migrated — Claude Code ships a built-in `skill-creator`; project also keeps the `make-skill-template` skill |
| `.github/instructions/memory-bank.instructions.DISABLED` | Cline-style Memory Bank driver (already disabled) | Deprecated — see §4 |
| `.github/skills/*` (liquibase, make-skill-template, mapstruct-hexagonal, spring-boot-data-ddd, spring-boot-java-cucumber, spring-boot-modulith, spring-mvc-controller-test) | Project Agent Skills (agentskills.io format) | **Migrated** (copied — format is identical) |
| `.cursor/.cursorrules` | Cursor rules pointing at `docs/ai-rules/` roles | Deprecated, not migrated — predates spec-workflow; contains broken file references (`docs/back-architecture.md`) |
| `docs/ai-rules/*.md` (ba-role, software-architecture-role, software-developer-role) | Legacy role prompts for Cursor | Deprecated — superseded by the SDD commands; kept as historical docs |
| `memory-bank/` | Legacy Memory Bank state | Deprecated — see §4 |
| `AGENTS.md` | Tool-agnostic agent guide (architecture, workflows, conventions) | **Kept** — imported by `CLAUDE.md` |
| `.spec-workflow/` templates | SDD document templates | **Kept** — referenced by the new commands |
| `architecture.md`, `overview.md`, `requirements/` | SDD context + spec output | **Kept** unchanged |

## 2. Migrated files (Claude equivalents)

| Source (Copilot/Cursor) | Destination (Claude) | Notes |
|---|---|---|
| `.github/agents/business-analyst.agent.md` | `.claude/commands/spec-requirements.md` | Slash command, not subagent: needs interactive approval (`AskUserQuestion`); Copilot `askQuestions` mapped accordingly |
| `.github/agents/architect.agent.md` | `.claude/commands/spec-design.md` | Slash command for consistency of the 4-step chain |
| `.github/agents/team-lead.agent.md` | `.claude/commands/spec-tasks.md` | Slash command by design (inline, user-supervised); spawns the `Explore` subagent via the Agent tool; `<skills>` directive repointed to `.claude/skills/` |
| `.github/agents/dev-manager.agent.md` + `.github/prompts/dev-manger-delegator.prompt.md` | `.claude/commands/spec-implement.md` | Orchestration loop runs in the main conversation and delegates each task to the `dev` subagent via the Agent tool |
| `.github/agents/dev.agent.md` | `.claude/agents/dev.md` | The single true subagent: tools restricted to `Read, Edit, Write, Glob, Grep, Bash`; Success/Failure reporting protocol preserved |
| `.github/prompts/project-architecture.prompt.md` | `.claude/commands/project-architecture.md` | `#tool:agent/runSubagent` → Agent tool (`subagent_type: "Explore"`); `{project}` bound to `$ARGUMENTS` |
| `.github/prompts/component-overview.prompt.md` | `.claude/commands/component-overview.md` | Same adaptations |
| `.github/prompts/update-docs.prompt.md` | `.claude/commands/update-docs.md` | — |
| `.github/skills/<7 skills>/` | `.claude/skills/<same names>/` | Copied verbatim (SKILL.md spec is shared); `copilot` author/branding references replaced with `claude` in `liquibase` and `make-skill-template` |
| `.github/instructions/java.instructions.md` | `.claude/skills/java-best-practices/SKILL.md` | `applyTo` glob frontmatter → skill `name`/`description` triggers |
| `.github/instructions/springboot.instructions.md` | `.claude/skills/spring-boot-best-practices/SKILL.md` | Same |
| `.github/instructions/github-actions-ci-cd-best-practices.instructions.md` | `.claude/skills/github-actions-ci-cd/SKILL.md` | Same |
| `.github/prompts/java-junit.prompt.md` | `.claude/skills/junit-best-practices/SKILL.md` | Best-practice prompt → on-demand skill |
| `.github/prompts/component-test.prompt.md` | `.claude/skills/component-test-conventions/SKILL.md` | Complements the `spring-boot-java-cucumber` skill with module structure/strategy |
| *(new)* | `CLAUDE.md` | Imports `AGENTS.md` via `@AGENTS.md`; documents the SDD command chain and obsolete locations |

## 3. Key migration decisions

1. **Commands vs subagents.** Copilot's five `.agent.md` files map to **four slash commands + one subagent**
   (`business-analyst`, `architect`, `team-lead`, `dev-manager` → `/spec-*` commands; `dev` → subagent). The only
   *hard* constraint is `business-analyst`: it must ask the user clarifying questions and get explicit plan approval,
   and subagents run autonomously with no way to prompt the user. The other three are a deliberate **design choice**,
   not a technical limit — they run inline in the main conversation so the user can watch and intervene in this
   user-supervised pipeline, they form a uniform `/spec-requirements → /spec-design → /spec-tasks → /spec-implement`
   chain, and their value is the files they write (not a returned summary, which is all an isolated subagent context
   would surface). *Correction (second pass):* an earlier draft justified this by claiming "Claude subagents cannot
   spawn other subagents" — that is **wrong**. As of Claude Code v2.1.172 subagents **can** nest (foreground at any
   depth), so `team-lead`/`dev-manager` *could* be subagents; they are commands by choice. `dev` remains the one
   subagent because it is a leaf executor where context isolation pays off — it grinds through a single task with heavy
   build/test output and returns only `Success:`/`Failure:`, keeping the orchestrator's context clean (input =
   task-file path; output = status line).
2. **Path-scoped instructions → skills + rules.** The long-form Java/Spring/JUnit/CI guidance became on-demand skills
   with trigger-rich descriptions (too long for always-on context). *Correction (second pass):* Claude Code **does**
   support `applyTo`-style glob scoping via `.claude/rules/*.md` with `paths:` frontmatter — see §6; the hard
   constraints were subsequently extracted into rules, with skills keeping the depth.
3. **`AGENTS.md` retained as the single source of project conventions.** `CLAUDE.md` imports it rather than duplicating
   it, so non-Claude tools can keep reading `AGENTS.md`.
4. **`dev-manager`'s `model: GPT-5 mini (copilot)` pin was dropped** — model selection is left to Claude Code defaults.
5. **`.spec-workflow/` templates, `architecture.md`, `overview.md`, `requirements/` are unchanged** — they are
   tool-agnostic and remain the backbone of the SDD process.

## 4. Memory Bank — marked obsolete

- **Location:** `memory-bank/` (state files + `tasks/`) and `.github/instructions/memory-bank.instructions.DISABLED`
  (the driver, already disabled before this migration).
- **Reason:** superseded by SDD documents under `requirements/`, by `AGENTS.md`/`CLAUDE.md` for durable conventions,
  and by Claude Code's built-in persistent memory. Hand-maintained memory state drifts and duplicates git history.
- **Marker:** `memory-bank/OBSOLETE.md` documents the deprecation and replacement mapping. Contents kept for history.

## 5. Cleanup status

- `.gitignore` now lists the deprecated Copilot/Cursor paths (`.github/agents/`, `.github/prompts/`,
  `.github/instructions/`, `.github/skills/`, `.github/copilot-instructions.md`, `.cursor/`) plus
  `.claude/settings.local.json`.
- **Note:** these legacy files are already tracked by git, so the ignore entries only prevent re-adding new files
  there; the tracked copies remain until removed with `git rm -r --cached <path>` (or deleted outright). Deletion was
  deliberately left as a follow-up decision once the Claude setup has been exercised on a real feature.
- No `.claudeignore` was created — Claude Code uses permission rules in `.claude/settings.json` rather than an ignore
  file; the "do not use" guidance for legacy paths lives in `CLAUDE.md`.

## 6. Rules layer (added 2026-06-12, second pass)

`.claude/rules/*.md` (officially supported, path-scoped via `paths:` frontmatter globs) now carries the short,
violation-prone **hard constraints**, loaded deterministically whenever matching files are touched — no reliance on
skill triggering:

| Rule | `paths` | Single source of truth for |
|---|---|---|
| `.claude/rules/java-style.md` | `**/*.java` | zero comments, records, constructor injection, immutability, naming |
| `.claude/rules/unit-tests.md` | `service/src/test/**` | `@ApiTest` (never bare `@WebMvcTest`), `@MockitoBean`, AssertJ-only, naming |
| `.claude/rules/component-tests.md` | `component-test/**` | happy-paths-only, `ScenarioContext`, BigDecimal comparisons, run command |
| `.claude/rules/liquibase.md` | `service/src/main/resources/db/changelog/**` | `TIMESTAMP WITH TIME ZONE`, changeset/file naming, no rollbacks |
| `.claude/rules/workflows.md` | `.github/workflows/**` | `docker` profile in CI tests, secrets hygiene; points to CI skill |

Division of labor: **CLAUDE.md** = workflow + project identity (always on), **rules** = short per-file-type MUSTs
(loaded on path match), **skills** = long-form how-to (loaded on demand).

To avoid drifting duplicates, the corresponding sections were **trimmed from `AGENTS.md`** ("Testing Conventions" and
"No Comments in Code" now point at the rule files). Note: the docs do not specify whether subagents inherit path-scoped
rules, so the SDD task files generated by `/spec-tasks` must keep spoon-feeding conventions to the `dev` subagent — do
not treat rules as the dev executor's only guardrail.
