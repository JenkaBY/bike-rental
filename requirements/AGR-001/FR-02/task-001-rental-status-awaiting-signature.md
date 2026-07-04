<task_file_template>

# Task 001: Add AWAITING_SIGNATURE status and transitions to RentalStatus

> **Applied Skill:** `java-best-practices` — enum constants, `Set.of()` immutable collections, zero inline
> comments. The transition map is the single source of truth for allowed lifecycle moves.

## 1. Objective

Introduce the new lifecycle constant `AWAITING_SIGNATURE` and wire up its allowed transitions:
`DRAFT → {ACTIVE, CANCELLED, AWAITING_SIGNATURE}` and `AWAITING_SIGNATURE → {ACTIVE, DRAFT}`. All other
transitions stay exactly as they are. This is the foundation every later task depends on.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/model/RentalStatus.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

No new imports are needed.

**Code to Add/Replace:**

### Change 3.1 — Add the `AWAITING_SIGNATURE` enum constant

* **Location:** The enum constant list currently reads:

  ```java
  public enum RentalStatus {
      DRAFT,
      ACTIVE,
      COMPLETED,
      CANCELLED,
      DEBT;
  ```

  Replace it exactly with:

* **Snippet:**

```java
public enum RentalStatus {
    DRAFT,
    AWAITING_SIGNATURE,
    ACTIVE,
    COMPLETED,
    CANCELLED,
    DEBT;
```

### Change 3.2 — Extend the transition map

* **Location:** Inside the `static {` initializer block, the current `map.put(DRAFT, ...)` line reads:

  ```java
          map.put(DRAFT, Set.of(ACTIVE, CANCELLED));
  ```

  Replace ONLY that single line, and add ONE new line for `AWAITING_SIGNATURE` directly beneath it, so the
  block reads exactly:

* **Snippet:**

```java
        map.put(DRAFT, Set.of(ACTIVE, CANCELLED, AWAITING_SIGNATURE));
        map.put(AWAITING_SIGNATURE, Set.of(ACTIVE, DRAFT));
```

  After the edit the initializer block must read exactly:

```java
    static {
        var map = new EnumMap<RentalStatus, Set<RentalStatus>>(RentalStatus.class);
        map.put(DRAFT, Set.of(ACTIVE, CANCELLED, AWAITING_SIGNATURE));
        map.put(AWAITING_SIGNATURE, Set.of(ACTIVE, DRAFT));
        map.put(ACTIVE, Set.of(CANCELLED));
        map.put(COMPLETED, Set.of());
        map.put(CANCELLED, Set.of());
        map.put(DEBT, Set.of());
        ALLOWED_TRANSITIONS = Map.copyOf(map);
    }
```

> Do NOT modify `validateTransitionTo(...)` — it already reads the map generically.

## 4. Validation Steps

Execute the following command to ensure this task was successful. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
