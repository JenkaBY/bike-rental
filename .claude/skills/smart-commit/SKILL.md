---
name: smart-commit
description: 'Analyse staged and unstaged git changes, group them into logical commits, draft meaningful commit messages that match the repo style, then stage and commit each group. Use when asked to "commit changes", "create a commit", "commit my changes", "write a commit message", or when the user says /smart-commit.'
---
# Smart Commit
Inspect the current working tree, infer the right commit granularity, and create well-messaged commits that follow this repository's conventions.
## When to Use This Skill
- User says `/smart-commit`, "commit my changes", "create a commit", or "commit these changes"
- User asks for a meaningful commit message for the current diff
- User wants changes split into multiple logical commits automatically
## Workflow
### Step 1 — Read context (run in parallel)
```bash
git status                        # which files changed
git diff HEAD                     # full diff (staged + unstaged)
git log --oneline -10             # recent messages → infer style/prefix convention
```
Step 2 — Analyse changes
Group the diff by logical concern. Ask yourself:
Do changes span unrelated features or layers? → separate commits.
Is there a mix of production code + tests for the same feature? → one commit.
Are there doc-only, config-only, or refactor-only changes? → separate commits.
Step 3 — Draft commit messages
Match the repo's prefix/style detected in Step 1 (e.g. feat:, fix:, improve:, doc:, chore:).
Rules:
Subject line ≤ 72 characters, imperative mood ("add", not "adds" / "added")
Focus on the why, not a file list
No trailing period on the subject line
If a body is useful, blank line after subject, then wrap at 72 chars
Step 4 — Stage and commit each group
For a single logical group — stage everything and commit:
git add <relevant files>
git commit -m "$(cat <<'EOF'
<subject line>
Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>
EOF
)"
For multiple groups — repeat per group, staging only that group's files each time.
Step 5 — Report
After committing, show the user:
Each commit hash + subject created
Files included in each commit
Commit Message Conventions in This Repo
Observed prefixes (from git log):
Prefix	When to use
feat:	New user-visible feature
fix:	Bug fix
improve:	Enhancement to existing feature
doc:	Documentation only
chore:	Build, dependency, or tooling changes
refactor:	Internal restructuring, no behaviour change
test:	Test additions or fixes only
If the repo uses a different convention (detected in Step 1), follow that instead.
Safety Rules
Never amend a commit that is already on a remote branch.
Never force-push without explicit user instruction.
Never skip pre-commit hooks (--no-verify).
If a hook fails, diagnose and fix before retrying — do not bypass.
Do not commit files that are likely secrets (.env, credentials, private keys). Warn the user if such files are staged.
If nothing is staged and no tracked files are modified, tell the user and stop.
Troubleshooting
Problem	Resolution
Pre-commit hook fails	Read the hook output, fix the underlying issue, then re-commit
Ambiguous grouping	Prefer fewer, broader commits over many micro-commits; ask user if unsure
No changes detected	Run git status and confirm working tree is clean; nothing to commit
User wants a single commit	Stage everything and create one commit without splitting
