# Task 002: Create RentalSpec and SpecConstant in Infrastructure

> **Applied Skill:** `spring-boot-data-ddd` — JPA Specification pattern;
> `java.instructions.md` — utility classes, annotation-based configuration

## 1. Objective

Create two new infrastructure files that implement the dynamic JPA query:

- `SpecConstant` — a `@UtilityClass` with field-name constants and the shared Instant format string.
- `RentalSpec` — an annotated `Specification<RentalJpaEntity>` interface using `@And`, `@Spec`,
  and `@Join` from `net.kaczmarzyk:specification-arg-resolver`, following the
  `CustomerTransactionsSpec` pattern exactly.

## 2. File to Modify / Create

### File A

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/specification/SpecConstant.java`
* **Action:** Create New File

```java
package com.github.jenkaby.bikerental.rental.infrastructure.persistence.specification;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SpecConstant {

    public static final String INSTANT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public class RentalField {
        public static final String STATUS = "status";
        public static final String CUSTOMER_ID = "customerId";
        public static final String CREATED_AT = "createdAt";
        public static final String EQUIPMENT_UID = "re.equipmentUid";
        public static final String PARAM_EQUIPMENT_UID = "equipmentUid";
        public static final String PARAM_CREATED_FROM = "createdFrom";
        public static final String PARAM_CREATED_TO = "createdTo";
    }
}
```

### File B

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/specification/RentalSpec.java`
* **Action:** Create New File

**Imports Required:**

```java
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import jakarta.persistence.criteria.JoinType;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LessThan;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;
```

```java
package com.github.jenkaby.bikerental.rental.infrastructure.persistence.specification;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import jakarta.persistence.criteria.JoinType;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LessThan;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

@Join(path = "rentalEquipments", alias = "re", joinType = JoinType.LEFT, distinct = true)
@And({
        @Spec(path = SpecConstant.RentalField.STATUS, params = SpecConstant.RentalField.STATUS, spec = Equal.class),
        @Spec(path = SpecConstant.RentalField.CUSTOMER_ID, params = SpecConstant.RentalField.CUSTOMER_ID, spec = Equal.class),
        @Spec(path = SpecConstant.RentalField.EQUIPMENT_UID, params = SpecConstant.RentalField.PARAM_EQUIPMENT_UID, spec = Equal.class),
        @Spec(path = SpecConstant.RentalField.CREATED_AT, params = SpecConstant.RentalField.PARAM_CREATED_FROM, spec = GreaterThanOrEqual.class, config = SpecConstant.INSTANT_FORMAT),
        @Spec(path = SpecConstant.RentalField.CREATED_AT, params = SpecConstant.RentalField.PARAM_CREATED_TO, spec = LessThan.class, config = SpecConstant.INSTANT_FORMAT),
})
public interface RentalSpec extends Specification<RentalJpaEntity> {
}
```

> **Note on `@Join(distinct = true)`:** Prevents duplicate `RentalJpaEntity` rows when the
> `rentalEquipments` join matches multiple rows — equivalent to the `SELECT DISTINCT` in the
> existing `findByStatusAndEquipmentUid` JPQL query. When `equipmentUid` param is absent the join
> predicate is omitted, but `distinct = true` still prevents potential Cartesian duplication.

## 4. Validation Steps

skip
