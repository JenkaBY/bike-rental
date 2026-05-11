# Task 005: Create `GetEquipmentsByConditionsUseCase` Interface and Service

> **Applied Skill:** `springboot.instructions.md` — Use case interface in `application/usecase/`; implementation in
`application/service/`; `@Service` with package-private class and constructor injection.

## 1. Objective

Create the use-case interface and its Spring `@Service` implementation. The service validates that
`conditions` is non-empty, then delegates to `EquipmentRepository.findByConditions`.

---

### Part A — Use Case Interface

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/application/usecase/GetEquipmentsByConditionsUseCase.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** (all in the snippet below)

**Code to Add/Replace:**

* **Location:** New file — full content below.

```java
package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.EquipmentSearchFilter;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.shared.domain.model.Condition;

import java.util.List;
import java.util.Set;

public interface GetEquipmentsByConditionsUseCase {

    List<Equipment> execute(Set<Condition> conditions, EquipmentSearchFilter filter);
}
```

---

### Part B — Service Implementation

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/application/service/GetEquipmentsByConditionsService.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:** (all in the snippet below)

**Code to Add/Replace:**

* **Location:** New file — full content below.

```java
package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.EquipmentSearchFilter;
import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentsByConditionsUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
class GetEquipmentsByConditionsService implements GetEquipmentsByConditionsUseCase {

    private final EquipmentRepository repository;

    GetEquipmentsByConditionsService(EquipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Equipment> execute(Set<Condition> conditions, EquipmentSearchFilter filter) {
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("conditions must not be empty");
        }
        return repository.findByConditions(conditions, filter.q());
    }
}
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
