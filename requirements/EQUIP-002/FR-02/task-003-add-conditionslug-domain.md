# Task 003: Add `conditionSlug` Field to `Equipment` Domain Aggregate

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — Domain model carries typed fields; no framework annotations in
> domain layer.

## 1. Objective

Add a `Condition conditionSlug` field to the `Equipment` domain aggregate so the physical condition
is part of the domain model and flows through the application layer.

**Note:** The aggregate already has a `private String condition` field (free-text notes). This field is
left unchanged. The new `conditionSlug` field carries the typed `Condition` enum value from the
`condition_slug` column.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/equipment/domain/model/Equipment.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
```

Add this import after the existing
`import com.github.jenkaby.bikerental.equipment.domain.service.StatusTransitionPolicy;` line.

**Code to Add/Replace:**

* **Location:** Inside the class body, immediately after the `private String statusSlug;` field and
  before the `private String model;` field. Current code to find the insertion point:

```java
    private String typeSlug;
    private String statusSlug;
    private String model;
    private LocalDate commissionedAt;
    private String condition;
```

Replace with:

```java
    private String typeSlug;
    private String statusSlug;
    private Condition conditionSlug;
    private String model;
    private LocalDate commissionedAt;
    private String condition;
```

## 4. Validation Steps

skip validation
