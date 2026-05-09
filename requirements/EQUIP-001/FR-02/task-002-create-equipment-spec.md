# Task 002: Create EquipmentSpec Interface

> **Applied Skill:** N/A (JPA Specification interface — mirrors `CustomerSpec` pattern from
> `customer/infrastructure/persistence/specification/CustomerSpec.java` and the `@And`/`@Or` nesting pattern
> demonstrated by `CustomerTransactionsSpec`)

## 1. Objective

Create the `EquipmentSpec` interface that declaratively composes all equipment search predicates:
exact-match filters for `status` and `type`, plus a case-insensitive substring OR group across `uid`,
`serialNumber`, and `model` — all bound via `params` to the HTTP query parameter names.

## 2. File to Modify / Create

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/persistence/specification/EquipmentSpec.java`
* **Action:** Create New File

## 3. Code Implementation

**Imports Required:**

```java
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;
```

**Code to Add/Replace:**

* **Location:** New file — full content below.

* **Snippet:**

```java
package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.specification;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

@And({
        @Spec(path = EquipmentSpecConstant.STATUS_SLUG, params = EquipmentSpecConstant.STATUS, spec = Equal.class),
        @Spec(path = EquipmentSpecConstant.TYPE_SLUG, params = EquipmentSpecConstant.TYPE, spec = Equal.class),
        @Or({
                @Spec(path = EquipmentSpecConstant.UID, params = EquipmentSpecConstant.SEARCH, spec = LikeIgnoreCase.class),
                @Spec(path = EquipmentSpecConstant.SERIAL_NUMBER, params = EquipmentSpecConstant.SEARCH, spec = LikeIgnoreCase.class),
                @Spec(path = EquipmentSpecConstant.MODEL, params = EquipmentSpecConstant.SEARCH, spec = LikeIgnoreCase.class)
        })
})
public interface EquipmentSpec extends Specification<EquipmentJpaEntity> {
}
```

> **Behaviour notes:**
> * When `status` param is absent/blank the `Equal` spec for `statusSlug` is omitted (library default).
> * When `type` param is absent/blank the `Equal` spec for `typeSlug` is omitted.
> * When `q` param is absent/blank the entire `@Or` group is omitted — no text filter is applied.
> * When `q` is present, the `@Or` produces
    `(uid ILIKE '%value%' OR serialNumber ILIKE '%value%' OR model ILIKE '%value%')`.
> * The `@And` at the top level AND-s all active clauses together.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

> Both `EquipmentSpecConstant` and `EquipmentSpec` compile cleanly. No other file references `EquipmentSpec` yet;
> that wiring is done in task 004.
