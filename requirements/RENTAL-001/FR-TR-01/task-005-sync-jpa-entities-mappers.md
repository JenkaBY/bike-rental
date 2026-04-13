# Task 005: Sync JPA Entities and Mappers with New Domain Fields

> **Applied Skill:** `.github/skills/spring-boot-data-ddd/SKILL.md` — JPA entity mapping, audit field ignoring  
> **Applied Skill:** `.github/skills/mapstruct-hexagonal/SKILL.md` — Pattern 4a (audit field ignoring), Pattern 1

## 1. Objective

Propagate the new domain fields into the JPA layer:

- `RentalJpaEntity` gains `specialTariffId`, `specialPrice`, `discountPercent`
- `RentalEquipmentJpaEntity` gains `equipmentTypeSlug` (NOT NULL, VARCHAR 100)
- `RentalJpaMapper` gains no new `@Mapping` lines (field names match column names via MapStruct conventions)
- `RentalEquipmentJpaMapper` gains no new `@Mapping` lines (same reason)

## 2. Files to Modify

### 2a. `RentalJpaEntity.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/entity/RentalJpaEntity.java`
* **Action:** Modify Existing File

**Imports Required:**

```java
import java.math.BigDecimal; // already present
```

**Code to Add/Replace:**

* **Location:** After the last field (`@Column(name = "updated_at") private Instant updatedAt;`), before the
  `getEstimatedCost()` method. Add:

```java
    @Column(name = "special_tariff_id")
    private Long specialTariffId;

    @Column(name = "special_price", precision = 19, scale = 4)
    private BigDecimal specialPrice;

    @Column(name = "discount_percent")
    private Integer discountPercent;
```

---

### 2b. `RentalEquipmentJpaEntity.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/entity/RentalEquipmentJpaEntity.java`
* **Action:** Modify Existing File

**Code to Add/Replace:**

* **Location:** After the existing `@Column(name = "tariff_id") private Long tariffId;` field. Add:

```java
    @Column(name = "equipment_type_slug", nullable = false, length = 100)
    private String equipmentTypeSlug;
```

---

### 2c. `RentalJpaMapper.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/mapper/RentalJpaMapper.java`
* **Action:** Modify Existing File

The mapper uses `MoneyMapper` for `BigDecimal ↔ Money` conversions. The three new fields (`specialTariffId`,
`discountPercent`, `specialPrice`) all follow MapStruct's default naming convention (field names match), so
MapStruct will wire them automatically. **However**, `specialPrice` is `BigDecimal` in the entity and `Money` in
the domain — MapStruct will use `MoneyMapper` automatically.

No code changes required in `RentalJpaMapper.java` if field names exactly match. Verify by compiling — a
`mapstruct.unmappedTargetPolicy=ERROR` build flag will fail if any field is unmapped.

---

### 2d. `RentalEquipmentJpaMapper.java`

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/mapper/RentalEquipmentJpaMapper.java`
* **Action:** Modify Existing File

`equipmentTypeSlug` is `String` on both sides — MapStruct maps it automatically by name. No change needed.

Verify by compiling.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

If MapStruct reports unmapped target fields in `RentalJpaMapper` or `RentalEquipmentJpaMapper`, add explicit
`@Mapping` annotations to silence them, e.g.:

```java
// Only needed if MapStruct cannot match by name:
@Mapping(target = "specialPrice", source = "rental.specialPrice")
RentalJpaEntity toEntity(Rental rental);
```
