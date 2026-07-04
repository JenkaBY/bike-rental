---
paths:
  - "**/*.java"
---

# Java Style — hard constraints

- **Zero inline comments.** Use expressive naming and self-documenting structures instead.
- **Records** for DTOs, commands, queries, events, and any immutable data carrier — never traditional classes.
- **Temporal fields in web request DTOs must carry `@DateTimeFormat`** declaring the accepted format
  (e.g. `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)` for `Instant`/`LocalDateTime`, `ISO.DATE` for
  `LocalDate`) — the frontend's generated API client services rely on it.
- **Constructor injection only.** Dependency fields `private final`; never field injection, never `@Value` (use
  `@ConfigurationProperties` records).
- **Immutability by default:** `final` where possible, `List.of()`/`Map.of()` for fixed data, `Stream.toList()`.
- **No `null` contracts:** return/accept `Optional<T>` for possibly-absent values; `Objects.requireNonNull` at
  boundaries.
- **`var`** only when the type is obvious from the right-hand side.
- Naming: Google Java style — `UpperCamelCase` types, `lowerCamelCase` members, `UPPER_SNAKE_CASE` constants; nouns for
  classes, verbs for methods; no abbreviations.
- **No hardcoded tunables in components.** Sizes, margins, resource paths, labels, format patterns and similar knobs
  live in a component-scoped `@ConfigurationProperties` record (defaults via `@DefaultValue`) — never as
  `private static final` constants sprinkled through the class.
- **Never hand-assemble a DTO of 3+ fields inside a service or facade method** — extract a MapStruct mapper (for
  records with nested parts, use `default` methods calling the generated entry/list mappers; MapStruct cannot write
  dotted nested targets into records). Fixture/sample data used by a service goes into a dedicated provider component.
- **Read expensive classpath resources once.** Fonts, templates and similar assets are loaded/cached as bytes in the
  constructor — never re-read from the classpath per method call.

Depth: `java-best-practices` and `spring-boot-best-practices` skills.
