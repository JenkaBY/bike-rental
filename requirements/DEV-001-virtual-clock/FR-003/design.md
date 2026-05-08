# System Design: FR-003 - Mutable Clock Behaviour (controller-internal)

## 1. Architectural Overview

The mutable clock behaviour that allows a dev/test operator to pin time to a fixed `Instant` is **not a standalone class
or separate file**. It is encapsulated entirely as a private static inner class — `SettableClock` — inside
`DevTimeController` (FR-004), which is the only new production source file for this feature.

`SettableClock` extends `java.time.Clock` and holds two pieces of atomic state: the currently active delegate `Clock` (
initially `Clock.systemUTC()`), and a boolean flag indicating whether the clock is currently pinned. `DevTimeController`
initialises a single `SettableClock` instance in its constructor, exposes it as the primary `Clock` bean (overriding the
system default from FR-002), and mutates it through PUT and DELETE HTTP endpoints. All business services that inject
`Clock` receive this same singleton instance and automatically observe any time changes.

## 2. Impacted Components

* **`DevTimeController` (single new file, `shared` module):** Contains the private static inner class
  `SettableClock extends Clock`. No separate file is created for this type. The mutable state lives entirely within the
  controller's compilation unit.

## 3. Abstract Data Schema Changes

No database entities are introduced. State is in-memory only; it does not survive restarts.

## 4. Component Contracts & Payloads

`SettableClock` (private inner class, not a public API) exposes the following methods used only within the controller:

| Method                | Behaviour                                                                                   |
|-----------------------|---------------------------------------------------------------------------------------------|
| `instant()`           | Returns the pinned `Instant` if fixed; otherwise delegates to `Clock.systemUTC().instant()` |
| `getZone()`           | Always returns `ZoneOffset.UTC`                                                             |
| `withZone(ZoneId)`    | Returns a view of this clock in the given zone                                              |
| `setFixed(Instant t)` | Atomically stores `Clock.fixed(t, UTC)` as the delegate; sets the fixed flag to `true`      |
| `reset()`             | Atomically restores `Clock.systemUTC()` as the delegate; sets the fixed flag to `false`     |
| `isFixed()`           | Returns the current value of the atomic fixed flag                                          |

Business services see only the standard `Clock` contract (`instant()`, `getZone()`, `withZone()`) — the mutation methods
are package-private or caller-accessible only within the same class.

## 5. Updated Interaction Sequence

**Setting virtual time:**

1. `DevTimeController` receives PUT; extracts `Instant`.
2. Calls `settableClock.setFixed(instant)`.
3. `SettableClock` atomically swaps the delegate to `Clock.fixed(instant, UTC)` and marks `fixed = true`.
4. All `clock.instant()` calls across the application return the pinned instant.

**Resetting to system time:**

1. `DevTimeController` receives DELETE.
2. Calls `settableClock.reset()`.
3. `SettableClock` atomically restores `Clock.systemUTC()` and marks `fixed = false`.
4. All `clock.instant()` calls return real system time.

**Normal read path:**

1. Any business service calls `clock.instant()`.
2. `SettableClock.instant()` reads the atomic delegate reference and delegates — no locking.

## 6. Non-Functional Architecture Decisions

* **Security & Auth:** `SettableClock`'s mutation methods are only reachable through the profile-gated
  `DevTimeController`. No external caller can invoke them directly.
* **Scale & Performance:** The delegate is stored in an `AtomicReference<Clock>` (compare-and-set semantics). Reads are
  lock-free. The fixed flag is an `AtomicBoolean`. Both operations are wait-free for readers.
