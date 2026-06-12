# ⚠️ OBSOLETE — Memory Bank approach is no longer in use

**Status:** Deprecated as of 2026-06-12 (Claude migration).

This directory implements the legacy Cline-style "Memory Bank" approach
(`projectbrief.md`, `productContext.md`, `activeContext.md`, `systemPatterns.md`, `techContext.md`, `progress.md`,
`tasks/`), which was driven by `.github/instructions/memory-bank.instructions.DISABLED` — already disabled before this
migration.

## Why it is obsolete

- The project moved to **Specification-Driven Development (SDD)** via the spec-workflow toolkit: requirements, designs,
  and task breakdowns live under `requirements/` and are produced by the `/spec-*` slash commands (see root
  `CLAUDE.md`).
- Project context that the Memory Bank duplicated is now maintained in `AGENTS.md` / `CLAUDE.md`, `architecture.md`,
  and `overview.md`.
- Claude Code maintains its own persistent memory mechanism, so a hand-maintained memory bank is redundant and drifts
  out of date.

## What to use instead

| Memory Bank file                  | Replacement                                  |
|-----------------------------------|----------------------------------------------|
| `projectbrief.md`, `productContext.md` | `README.md`, `overview.md`, `docs/`     |
| `systemPatterns.md`, `techContext.md`  | `AGENTS.md` / `CLAUDE.md`, `architecture.md`, `.claude/skills/` |
| `activeContext.md`, `progress.md`      | git history, PRs                        |
| `tasks/`                               | `requirements/[REQ_ID]/` SDD documents  |

The contents are kept for historical reference only. Do not update files here and do not load them as agent context.
