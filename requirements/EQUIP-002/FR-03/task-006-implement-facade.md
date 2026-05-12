# Task 006: Implement `getEquipmentsByConditions` in `EquipmentFacadeImpl`

> **Applied Skill:** `spring-boot-modulith/SKILL.md` — Facade implementation wires use case + mapper; package-private
> class; no new public types introduced.

## 1. Objective

Implement the `getEquipmentsByConditions` method in `EquipmentFacadeImpl`, delegating to the new
`GetEquipmentsByConditionsUseCase` and mapping the result to `List<EquipmentInfo>` via the existing
`EquipmentToInfoMapper`.

## 2. File to Modify / Create

* **File Path:** `service/src/main/java/com/github/jenkaby/bikerental/equipment/EquipmentFacadeImpl.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentsByConditionsUseCase;
import com.github.jenkaby.bikerental.shared.domain.model.Condition;
import java.util.Set;
```

Add these imports after the existing
`import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByIdsUseCase;` line.

**Code to Add/Replace:**

* **Step 1 — Add the new field.** Locate the existing field declarations:

```java
    private final GetEquipmentByIdUseCase getEquipmentByIdUseCase;
    private final GetEquipmentByIdsUseCase getEquipmentByIdsUseCase;
    private final EquipmentToInfoMapper equipmentToInfoMapper;
```

Replace with:

```java
    private final GetEquipmentByIdUseCase getEquipmentByIdUseCase;
    private final GetEquipmentByIdsUseCase getEquipmentByIdsUseCase;
    private final GetEquipmentsByConditionsUseCase getEquipmentsByConditionsUseCase;
    private final EquipmentToInfoMapper equipmentToInfoMapper;
```

* **Step 2 — Update the constructor.** Locate the existing constructor:

```java
    EquipmentFacadeImpl(GetEquipmentByIdUseCase getEquipmentByIdUseCase,
                        GetEquipmentByIdsUseCase getEquipmentByIdsUseCase,
            EquipmentToInfoMapper equipmentToInfoMapper) {
        this.getEquipmentByIdUseCase = getEquipmentByIdUseCase;
        this.getEquipmentByIdsUseCase = getEquipmentByIdsUseCase;
        this.equipmentToInfoMapper = equipmentToInfoMapper;
    }
```

Replace with:

```java
    EquipmentFacadeImpl(GetEquipmentByIdUseCase getEquipmentByIdUseCase,
                        GetEquipmentByIdsUseCase getEquipmentByIdsUseCase,
                        GetEquipmentsByConditionsUseCase getEquipmentsByConditionsUseCase,
                        EquipmentToInfoMapper equipmentToInfoMapper) {
        this.getEquipmentByIdUseCase = getEquipmentByIdUseCase;
        this.getEquipmentByIdsUseCase = getEquipmentByIdsUseCase;
        this.getEquipmentsByConditionsUseCase = getEquipmentsByConditionsUseCase;
        this.equipmentToInfoMapper = equipmentToInfoMapper;
    }
```

* **Step 3 — Add the method implementation.** Add the following method after the closing `}` of the
  existing `findByIds` method and before the class's closing `}`:

```java
    @Override
    public List<EquipmentInfo> getEquipmentsByConditions(Set<Condition> conditions, EquipmentSearchFilter filter) {
        return getEquipmentsByConditionsUseCase.execute(conditions, filter)
                .stream()
                .map(equipmentToInfoMapper::toEquipmentInfo)
                .toList();
    }
```

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Full clean build including all equipment module tests:

```bash
./gradlew :service:test "-Dspring.profiles.active=test" --tests "com.github.jenkaby.bikerental.equipment.*"
```
