# Task 003: Add `findByConditions` to `EquipmentRepository` Domain Port

> **Applied Skill:** `spring-boot-data-ddd/SKILL.md` — Domain port (interface) in `domain/repository/`; no Spring
> annotations; accepts only domain types.

## 1. Objective

Add the `findByConditions` method to the `EquipmentRepository` domain port so the application layer
can query equipment by physical condition and optional free-text search.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/domain/repository/EquipmentRepository.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
import java.util.Set;
```

Add these imports after the existing `import java.util.Collection;` line.

**Code to Add/Replace:**

* **Location:** Inside the `EquipmentRepository` interface body, after the existing `findByUid` method.
  Current last two lines of the interface (find this exact text):

```java
    Optional<Equipment> findBySerialNumber(SerialNumber serialNumber);

    Optional<Equipment> findByUid(Uid uid);
}
```

Replace with:

```java
    Optional<Equipment> findBySerialNumber(SerialNumber serialNumber);

    Optional<Equipment> findByUid(Uid uid);

    List<Equipment> findByConditions(Set<Condition> conditions, String searchText);
}
```

## 4. Validation Steps

skip validation
