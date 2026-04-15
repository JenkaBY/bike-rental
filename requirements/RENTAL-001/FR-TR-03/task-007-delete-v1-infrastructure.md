# Task 007: Delete V1 Infrastructure Stack

> **Applied Skill:** `spring-boot-data-ddd` — Once all application-layer consumers are gone, the
> V1 JPA entity, Spring Data repository, domain repository port, adapter, and MapStruct entity
> mapper have no remaining callers. Deleting them is required to allow subsequent deletion of the
> `Tariff` domain model in task-008.

## 1. Objective

Delete the five V1 persistence infrastructure artefacts: the `TariffJpaEntity` (`@Entity` bound to
the `tariffs` table), its Spring Data JPA interface, the domain repository port `TariffRepository`,
the hexagonal adapter `TariffRepositoryAdapter`, and the MapStruct entity mapper `TariffJpaMapper`.

After this task the `Tariff` domain model (`Tariff.java`, `TariffStatus.java`) has no remaining
callers anywhere in the project — they will be deleted in task-008.

> **Prerequisite:** task-006 must be completed first; all application services that injected
> `TariffRepository` are already deleted.

## 2. Files to Delete

All paths are relative to `service/src/main/java/com/github/jenkaby/bikerental/tariff/`.

### Domain repository port

| File                                      | Why deleted                                                              |
|-------------------------------------------|--------------------------------------------------------------------------|
| `domain/repository/TariffRepository.java` | Interface that `TariffRepositoryAdapter` implements; references `Tariff` |

### JPA entity

| File                                                     | Why deleted                                                           |
|----------------------------------------------------------|-----------------------------------------------------------------------|
| `infrastructure/persistence/entity/TariffJpaEntity.java` | `@Entity @Table(name = "tariffs")` — maps to the now-deleted V1 table |

### Spring Data JPA repository

| File                                                             | Why deleted                                    |
|------------------------------------------------------------------|------------------------------------------------|
| `infrastructure/persistence/repository/TariffJpaRepository.java` | `extends JpaRepository<TariffJpaEntity, Long>` |

### Hexagonal adapter

| File                                                              | Why deleted                                         |
|-------------------------------------------------------------------|-----------------------------------------------------|
| `infrastructure/persistence/adapter/TariffRepositoryAdapter.java` | `@Repository` class implementing `TariffRepository` |

### MapStruct entity mapper

| File                                                     | Why deleted                                   |
|----------------------------------------------------------|-----------------------------------------------|
| `infrastructure/persistence/mapper/TariffJpaMapper.java` | `@Mapper` — maps `TariffJpaEntity` ↔ `Tariff` |

## 3. Code Implementation

No code is written; all actions are **file deletions**.

Absolute paths to delete (5 files):

```
service/src/main/java/com/github/jenkaby/bikerental/tariff/domain/repository/TariffRepository.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/infrastructure/persistence/entity/TariffJpaEntity.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/infrastructure/persistence/repository/TariffJpaRepository.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/infrastructure/persistence/adapter/TariffRepositoryAdapter.java
service/src/main/java/com/github/jenkaby/bikerental/tariff/infrastructure/persistence/mapper/TariffJpaMapper.java
```

> **Do NOT delete:** V2 infrastructure counterparts — `TariffV2JpaEntity.java`,
> `TariffV2JpaRepository.java`, `TariffV2Repository.java`, `TariffV2RepositoryAdapter.java`,
> `TariffV2JpaMapper.java` — all must remain.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```

Expected: BUILD SUCCESSFUL — `Tariff.java` and `TariffStatus.java` still exist at this point and
no compile error occurs. The V2 infrastructure stack compiles without issues.
