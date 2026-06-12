---
paths:
  - ".github/workflows/**"
---

# GitHub Actions Workflows — hard constraints

- Test runs in CI must add the `docker` profile alongside `test` to enable Liquibase
  (the plain `test` profile disables it).
- No secrets in workflow files — use GitHub secrets/environments.
- Pin third-party actions to a major version at minimum.

Depth: `github-actions-ci-cd` skill (workflow structure, caching, matrix strategies, deployment).
