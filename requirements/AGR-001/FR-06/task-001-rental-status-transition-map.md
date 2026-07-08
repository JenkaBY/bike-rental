<task_file_template>

# Task 001: Remove DRAFT → ACTIVE from the RentalStatus transition map

> **Applied Skill:** `java-best-practices` — minimal, surgical change to an existing `static` initializer; no inline
> comments added. This is the domain-layer enforcement of fr.md's rule "The domain transition `DRAFT → ACTIVE` is
> removed" (design.md §2, `RentalStatus`).

## 1. Objective

Remove `ACTIVE` from the set of allowed transition targets for `DRAFT` in `RentalStatus.ALLOWED_TRANSITIONS`, so the
domain no longer permits a direct `DRAFT → ACTIVE` transition. The `AWAITING_SIGNATURE` transition-map entry is
explicitly out of scope and must not be touched.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/rental/domain/model/RentalStatus.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None — no new imports needed.

**Code to Add/Replace:**

* **Location:** Inside the `static` initializer block, the line that populates the `DRAFT` entry of `map`.
* **Snippet:**

Replace this exact line:

```java
        map.put(DRAFT, Set.of(ACTIVE, CANCELLED, AWAITING_SIGNATURE));
```

with:

```java
        map.put(DRAFT, Set.of(CANCELLED, AWAITING_SIGNATURE));
```

Do NOT change any other line in this file — in particular leave
`map.put(AWAITING_SIGNATURE, Set.of(ACTIVE, DRAFT, CANCELLED));` exactly as-is.

## 4. Validation Steps

Execute the following command. Do NOT run the full application server.

```bash
./gradlew :service:compileJava "-Dspring.profiles.active=test"
```

</task_file_template>
