# Task 001: Add `EQUIPMENT_NOT_AVAILABLE` Constant to `ErrorCodes`

> **Applied Skill:** `d:\Projects\private\bikerent\.github\instructions\springboot.instructions.md` — Shared error code
> constants live in `ErrorCodes` utility class; naming follows the `module.domain_event` pattern already established in
> the file.

## 1. Objective

Add the new string constant `EQUIPMENT_NOT_AVAILABLE` to the `ErrorCodes` utility class so it can be referenced by both
the new `EquipmentOccupiedException` (Task 002) and the `RentalRestControllerAdvice` handler (Task 004) without using
raw strings.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/shared/web/advice/ErrorCodes.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:** None — the class already has no imports.

**Code to Add/Replace:**

* **Location:** Inside the `ErrorCodes` class body, immediately **after** the line
  `public static final String HOLD_REQUIRED = "rental.hold.required";`
* **Snippet:**

```java
    public static final String EQUIPMENT_NOT_AVAILABLE = "rental.equipment.not_available";
```

**Resulting section after modification:**

```java
    public static final String INSUFFICIENT_FUNDS = "rental.insufficient_funds";
    public static final String HOLD_REQUIRED = "rental.hold.required";
    public static final String EQUIPMENT_NOT_AVAILABLE = "rental.equipment.not_available";
```

## 4. Validation Steps

skip
