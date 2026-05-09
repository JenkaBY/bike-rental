# Task 001: Remove Dead uid and serialNumber Fields from SearchEquipmentsRequest

> **Applied Skill:** N/A (plain Java record field removal — no framework pattern required)

## 1. Objective

Remove the `uid` and `serialNumber` record components from `SearchEquipmentsRequest`. These fields were never
wired into the controller or any other production component and are superseded by the `q` parameter introduced
in FR-01.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/web/query/dto/SearchEquipmentsRequest.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

None — no import changes needed.

**Code to Add/Replace:**

* **Location:** Replace the entire record body of `SearchEquipmentsRequest`.

* **Current code:**

```java
public record SearchEquipmentsRequest(
        String status,
        String type,
        String serialNumber,
        String uid
) {
}
```

* **Snippet (replace with):**

```java
public record SearchEquipmentsRequest(
        String status,
        String type
) {
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

> Compilation must succeed with zero errors. No production class references `.uid()` or `.serialNumber()` on
> a `SearchEquipmentsRequest` instance, so no cascading changes are required.
