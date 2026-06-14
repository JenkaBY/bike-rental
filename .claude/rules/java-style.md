---
paths:
  - "**/*.java"
---

# Java Style — hard constraints

- **Zero inline comments.** Use expressive naming and self-documenting structures instead.
- **Records** for DTOs, commands, queries, events, and any immutable data carrier — never traditional classes.
- **Constructor injection only.** Dependency fields `private final`; never field injection, never `@Value` (use
  `@ConfigurationProperties` records).
- **Immutability by default:** `final` where possible, `List.of()`/`Map.of()` for fixed data, `Stream.toList()`.
- **No `null` contracts:** return/accept `Optional<T>` for possibly-absent values; `Objects.requireNonNull` at
  boundaries.
- **`var`** only when the type is obvious from the right-hand side.
- Naming: Google Java style — `UpperCamelCase` types, `lowerCamelCase` members, `UPPER_SNAKE_CASE` constants; nouns for
  classes, verbs for methods; no abbreviations.

Depth: `java-best-practices` and `spring-boot-best-practices` skills.
