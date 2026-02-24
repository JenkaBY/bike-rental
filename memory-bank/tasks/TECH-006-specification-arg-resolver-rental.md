# [TECH-006] Integrate specification-arg-resolver for rental filtering

**Status:** Pending  
**Added:** 2026-02-18  
**Updated:** 2026-02-18  
**Priority:** Medium  
**Module:** rental (query layer)  
**Type:** Technical Debt / Improvement  
**Dependencies:** None

## Original Request

Integrate `net.kaczmarzyk:specification-arg-resolver` library into `RentalQueryController` to replace manual filtering
logic in `FindRentalsService` with declarative Specifications resolved from HTTP query parameters.

## Problem Statement

**Current Situation:**

- `RentalQueryController` accepts `status` and `customerId` as `@RequestParam` values
- `FindRentalsService` contains manual conditional logic to determine which repository method to call:
  ```java
  if (query.customerId() != null && query.status() != null) {
      return repository.findByStatusAndCustomerId(...);
  } else if (query.customerId() != null) {
      return repository.findByCustomerId(...);
  } else {
      return repository.findByStatus(...);
  }
  ```
- Each new filter combination requires:
    - New repository method
    - New conditional branch in service
    - New JPA repository method
    - Additional adapter implementation

**Issues with Current Approach:**

1. **Scalability Problem** - Adding new filters (e.g., `equipmentId`, date ranges, cost ranges) requires exponential
   growth of repository methods
2. **Code Duplication** - Similar filtering logic repeated across multiple repository methods
3. **Maintenance Burden** - Complex conditional logic becomes harder to maintain as filters grow
4. **Limited Flexibility** - Cannot easily combine filters or add complex conditions (OR, ranges, LIKE)
5. **Testing Overhead** - Each filter combination requires separate test cases
6. **Inconsistency** - Different approach from equipment filtering (which already uses specification-arg-resolver per
   TECH-002)

## Solution Approach

Integrate `specification-arg-resolver` library with proper architectural layering: controller accepts DTO search
criteria via `@ParameterObject`, which are then transformed into JPA Specifications in the infrastructure layer using a
dedicated mapper.

**Benefits:**

- ✅ Proper architectural separation (no JPA entities in controller)
- ✅ DTO-based search criteria (type-safe, testable)
- ✅ Declarative Specification building via `RentalSpecificationMapper`
- ✅ Support for complex filters (AND/OR, ranges, LIKE, IN)
- ✅ Reduced code duplication
- ✅ Easier to add new filters without new repository methods
- ✅ Better testability (test Specifications and DTOs separately)
- ✅ Maintainable and scalable approach

**Recommended Approach:**

1. Create `RentalSearchCriteria` DTO with filter fields
2. Controller accepts `@ParameterObject @Nullable RentalSearchCriteria` and `@ParameterObject Pageable`
3. Transform criteria DTO to domain query object
4. In repository adapter, use `RentalSpecificationMapper` to convert search criteria DTO to JPA
   `Specification<RentalJpaEntity>`
5. Use `SpecificationBuilder` pattern for building Specifications
6. Call `JpaSpecificationExecutor.findAll(spec, pageable)` in adapter

## Implementation Plan

### Phase 1: Dependency and Repository Setup

- [ ] Add `specification-arg-resolver` dependency to `service/build.gradle` (if not already added for TECH-002)
- [ ] Update `RentalJpaRepository` to extend `JpaSpecificationExecutor<RentalJpaEntity>`
- [ ] Verify `SpecificationArgumentResolver` is registered in WebMvc config (should be automatic)

### Phase 2: Create Search Criteria DTO

- [ ] Create `RentalSearchCriteria` record/DTO in `rental.web.query.dto` package:
  ```java
  public record RentalSearchCriteria(
      RentalStatus status,
      UUID customerId,
      Long equipmentId  // future
  ) {}
  ```
- [ ] Add validation annotations if needed (`@Nullable`, etc.)
- [ ] Document fields and their usage

### Phase 3: Controller Refactoring

- [ ] Update `RentalQueryController.getRentals()` to accept:
  ```java
  @ParameterObject @Nullable RentalSearchCriteria rentalSearchCriteria,
  @ParameterObject Pageable pageableRequest
  ```
- [ ] Remove manual `@RequestParam` declarations
- [ ] Transform `RentalSearchCriteria` to `FindRentalsUseCase.FindRentalsQuery`
- [ ] Update logging to reflect new approach

### Phase 4: Create Specification Mapper

- [ ] Create `RentalSpecificationMapper` class in `rental.infrastructure.persistence.mapper` package
- [ ] Implement `fromSearchCriteria(@Nullable RentalSearchCriteria searchCriteria)` method
- [ ] Use `SpecificationBuilder` pattern to build `Specification<RentalJpaEntity>`:
  ```java
  public Specification<RentalJpaEntity> fromSearchCriteria(@Nullable RentalSearchCriteria searchCriteria) {
      var criteriaOptional = Optional.ofNullable(searchCriteria);
      
      return SpecificationBuilder.specification(AND)
          .withParam(SpecConst.RentalParam.STATUS,
                  criteriaOptional.map(RentalSearchCriteria::status).orElse(null))
          .withParam(SpecConst.RentalParam.CUSTOMER_ID,
                  criteriaOptional.map(RentalSearchCriteria::customerId).orElse(null))
          .build();
  }
  ```
- [ ] Create `SpecConst.RentalParam` constants class for parameter names
- [ ] Handle null values gracefully (optional filters)

### Phase 5: Repository Adapter Update

- [ ] Add method `findAll(Specification<RentalJpaEntity> spec, PageRequest pageRequest)` to `RentalRepository` interface
- [ ] Inject `RentalSpecificationMapper` into `RentalRepositoryAdapter`
- [ ] Implement `findAll` method in adapter:
  ```java
  var springPageRequest = pageMapper.toSpring(pageRequest);
  var page = repository.findAll(spec, springPageRequest);
  return pageMapper.toDomain(page).map(mapper::toDomain);
  ```
- [ ] Update `FindRentalsService` to use Specification-based approach:
    - Remove conditional logic
    - Accept `RentalSearchCriteria` in query (or convert to Specification in adapter)
    - Call `repository.findAll(spec, pageRequest)` where spec is built from criteria

### Phase 6: Use Case and Query Refactoring

- [ ] Update `FindRentalsUseCase.FindRentalsQuery` to accept `RentalSearchCriteria` instead of individual filter fields
- [ ] Update `FindRentalsService` to pass criteria to repository adapter
- [ ] Repository adapter converts criteria to Specification using mapper
- [ ] Domain layer remains clean (no JPA dependencies)

### Phase 7: Testing

- [ ] Update `FindRentalsServiceTest` to test with Specifications
- [ ] Update `RentalQueryControllerTest` to verify Specification resolution
- [ ] Add component tests for various filter combinations:
    - Single filters (status only, customerId only)
    - Combined filters (status + customerId)
    - Complex filters (date ranges, multiple statuses)
- [ ] Verify backward compatibility with existing API clients

### Phase 8: Documentation

- [ ] Document new query parameter syntax
- [ ] Update API documentation
- [ ] Add examples of advanced filtering
- [ ] Update memory-bank progress

## Technical Details

### Dependency

If not already added for TECH-002:

```gradle
implementation "net.kaczmarzyk:specification-arg-resolver:4.0.0"
```

### Search Criteria DTO Example

```java
package com.github.jenkaby.bikerental.rental.web.query.dto;

import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public record RentalSearchCriteria(
        @Nullable RentalStatus status,
        @Nullable UUID customerId,
        @Nullable Long equipmentId  // future
) {
}
```

### Controller Example

```java

@GetMapping
public ResponseEntity<Page<RentalSummaryResponse>> getRentals(
        @ParameterObject @Nullable RentalSearchCriteria rentalSearchCriteria,
        @ParameterObject @PageableDefault(size = 20, sort = "expectedReturnAt", direction = Sort.Direction.ASC) Pageable pageableRequest) {

    log.info("[GET] Get rentals with filters: {}", rentalSearchCriteria);

    PageRequest pageRequest = pageMapper.toPageRequest(pageableRequest);
    var query = new FindRentalsUseCase.FindRentalsQuery(rentalSearchCriteria, pageRequest);
    Page<Rental> rentals = findRentalsUseCase.execute(query);
    Page<RentalSummaryResponse> response = rentals.map(mapper::toRentalSummaryResponse);
    return ResponseEntity.ok(response);
}
```

### Specification Mapper Example

```java
package com.github.jenkaby.bikerental.rental.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import com.github.jenkaby.bikerental.rental.web.query.dto.RentalSearchCriteria;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

import static com.github.jenkaby.bikerental.rental.infrastructure.persistence.spec.SpecConst.RentalParam;
import static com.github.jenkaby.bikerental.shared.persistence.spec.SpecificationBuilder.specification;
import static com.github.jenkaby.bikerental.shared.persistence.spec.SpecificationBuilder.Operator.AND;

public class RentalSpecificationMapper {

    public Specification<RentalJpaEntity> fromSearchCriteria(@Nullable RentalSearchCriteria searchCriteria) {
        var criteriaOptional = Optional.ofNullable(searchCriteria);

        return specification(AND)
                .withParam(RentalParam.STATUS,
                        criteriaOptional.map(RentalSearchCriteria::status)
                                .map(Enum::name)
                                .orElse(null))
                .withParam(RentalParam.CUSTOMER_ID,
                        criteriaOptional.map(RentalSearchCriteria::customerId)
                                .map(UUID::toString)
                                .orElse(null))
                .build();
    }
}
```

### SpecConst Constants Example

```java
package com.github.jenkaby.bikerental.rental.infrastructure.persistence.spec;

public class SpecConst {
    public static class RentalParam {
        public static final String STATUS = "status";
        public static final String CUSTOMER_ID = "customerId";
        public static final String EQUIPMENT_ID = "equipmentId";
    }
}
```

### Repository Interface Update

```java
public interface RentalRepository {
    // ... existing methods ...
    Page<Rental> findAll(RentalSearchCriteria searchCriteria, PageRequest pageRequest);
}
```

### Adapter Implementation

```java

@Repository
class RentalRepositoryAdapter implements RentalRepository {

    private final RentalJpaRepository repository;
    private final RentalJpaMapper mapper;
    private final PageMapper pageMapper;
    private final RentalSpecificationMapper specMapper;

    // ... constructor ...

    @Override
    public Page<Rental> findAll(RentalSearchCriteria searchCriteria, PageRequest pageRequest) {
        Specification<RentalJpaEntity> spec = specMapper.fromSearchCriteria(searchCriteria);
        var springPageRequest = pageMapper.toSpring(pageRequest);
        var page = repository.findAll(spec, springPageRequest);
        return pageMapper.toDomain(page).map(mapper::toDomain);
    }
}
```

### Use Case Query Update

```java
public interface FindRentalsUseCase {
    Page<Rental> execute(FindRentalsQuery query);

    record FindRentalsQuery(
            RentalSearchCriteria searchCriteria,
            PageRequest pageRequest
    ) {
    }
}
```

### Service Implementation Update

```java

@Override
public Page<Rental> execute(FindRentalsQuery query) {
    return repository.findAll(query.searchCriteria(), query.pageRequest());
}
```

### Future Filter Support

After implementation, easy to add:

- `equipmentId` filter: Add field to `RentalSearchCriteria`, add param to `SpecConst.RentalParam`, add `.withParam()` in
  mapper
- Date ranges: Add `startedAtFrom`, `startedAtTo` to criteria, use `GreaterThanOrEqual`/`LessThanOrEqual` in
  SpecificationBuilder
- Multiple statuses: Add `statuses` (List) to criteria, use `In` specification
- Cost ranges: Add `costMin`, `costMax` to criteria, use range specifications

## Files & Components to Change

**New Files:**

- `service/src/main/java/.../rental/web/query/dto/RentalSearchCriteria.java` (new DTO)
- `service/src/main/java/.../rental/infrastructure/persistence/mapper/RentalSpecificationMapper.java` (new mapper)
- `service/src/main/java/.../rental/infrastructure/persistence/spec/SpecConst.java` (new constants class)
- `service/src/main/java/.../shared/persistence/spec/SpecificationBuilder.java` (if not exists, utility for building
  Specifications - **needs to be created**)

**Modified Files:**

- `service/build.gradle` (add dependency if not present)
- `service/src/main/java/.../rental/infrastructure/persistence/repository/RentalJpaRepository.java` (extend
  `JpaSpecificationExecutor<RentalJpaEntity>`)
- `service/src/main/java/.../rental/domain/repository/RentalRepository.java` (add
  `findAll(RentalSearchCriteria, PageRequest)`)
- `service/src/main/java/.../rental/infrastructure/persistence/adapter/RentalRepositoryAdapter.java` (inject mapper,
  implement `findAll` with Specification)
- `service/src/main/java/.../rental/application/usecase/FindRentalsUseCase.java` (update query to accept
  `RentalSearchCriteria`)
- `service/src/main/java/.../rental/application/service/FindRentalsService.java` (simplify to pass criteria to
  repository)
- `service/src/main/java/.../rental/web/query/RentalQueryController.java` (use `@ParameterObject RentalSearchCriteria`)
- `service/src/test/java/.../rental/application/service/FindRentalsServiceTest.java` (update tests)
- `service/src/test/java/.../rental/web/query/RentalQueryControllerTest.java` (update tests)
- `service/src/test/java/.../rental/infrastructure/persistence/mapper/RentalSpecificationMapperTest.java` (new test)
- `component-test/src/test/resources/features/rental/rental-query.feature` (verify existing scenarios still work)

## Migration Strategy

**Backward Compatibility:**

- Keep existing query parameters (`status`, `customerId`) working
- `@Spec` annotations will automatically map these parameters
- No breaking changes to API contract
- Existing clients continue to work without modification

**Rollback Plan:**

- Remove dependency from `build.gradle`
- Revert controller to use `@RequestParam`
- Restore conditional logic in `FindRentalsService`
- Restore individual repository methods

## Estimated Effort

~1.5-2 days (one engineer) including:

- Implementation: 4-6 hours
- Testing: 3-4 hours
- Documentation: 1-2 hours

## Subtasks (checklist)

- [ ] Add dependency to `service/build.gradle` (if not already present)
- [ ] **Create `SpecificationBuilder` utility class** (if not exists) in `shared.persistence.spec` package following the
  pattern:
  ```java
  public class SpecificationBuilder {
      public enum Operator { AND, OR }
      
      public static SpecificationBuilder specification(Operator operator) { ... }
      
      public SpecificationBuilder withParam(String paramName, String paramValue) { ... }
      
      public <T> Specification<T> build() { ... }
  }
  ```
- [ ] Create `RentalSearchCriteria` DTO with filter fields
- [ ] Create `SpecConst.RentalParam` constants class
- [ ] Create `RentalSpecificationMapper` class
- [ ] Implement `fromSearchCriteria` method using `SpecificationBuilder` pattern
- [ ] Extend `RentalJpaRepository` with `JpaSpecificationExecutor<RentalJpaEntity>`
- [ ] Add `findAll(RentalSearchCriteria, PageRequest)` to `RentalRepository` interface
- [ ] Inject `RentalSpecificationMapper` into `RentalRepositoryAdapter`
- [ ] Implement `findAll` in `RentalRepositoryAdapter` (convert criteria to spec, call JPA repository)
- [ ] Update `FindRentalsUseCase.FindRentalsQuery` to accept `RentalSearchCriteria`
- [ ] Simplify `FindRentalsService` to pass criteria to repository
- [ ] Update `RentalQueryController` to use `@ParameterObject RentalSearchCriteria`
- [ ] Remove old repository methods (`findByStatus`, `findByCustomerId`, `findByStatusAndCustomerId`) or mark as
  deprecated
- [ ] Write unit tests for `RentalSpecificationMapper`
- [ ] Update unit tests for `FindRentalsService`
- [ ] Update WebMvc tests for `RentalQueryController`
- [ ] Verify component tests still pass
- [ ] Add component tests for advanced filtering scenarios
- [ ] Update API documentation
- [ ] Update memory-bank progress and task index

## Notes / Gotchas

- **Architectural Layering**: JPA `Specification` stays in infrastructure layer (adapter), never exposed to controller
  or domain
- **DTO Pattern**: `RentalSearchCriteria` is a DTO that can be easily tested and validated
- **SpecificationBuilder**: Need to implement or use existing `SpecificationBuilder` utility class (check if exists in
  shared module)
- **Field Names**: SpecificationBuilder `withParam` values must match JPA entity field names (e.g., `customerId`,
  `status`)
- **Parameter Binding**: `@ParameterObject` automatically binds query parameters to DTO fields
- **Null Handling**: `Optional.ofNullable()` pattern handles null criteria gracefully
- **Type Conversion**: May need to convert enum to string, UUID to string in mapper (depending on SpecificationBuilder
  implementation)
- **Default Sorting**: `@PageableDefault` works with `@ParameterObject Pageable`
- **Security**: Only expose allowed filterable fields in `RentalSearchCriteria` DTO

## Related Tasks

- [TECH-002] Integrate specification-arg-resolver for equipment filtering - Similar implementation for equipment module
- [US-RN-009] Просмотр активных аренд - Current implementation uses manual filtering

## Acceptance Criteria

- [ ] `specification-arg-resolver` dependency added (if not present)
- [ ] `RentalSearchCriteria` DTO created with filter fields
- [ ] `RentalSpecificationMapper` implemented using `SpecificationBuilder` pattern
- [ ] `RentalJpaRepository` extends `JpaSpecificationExecutor<RentalJpaEntity>`
- [ ] `RentalQueryController` uses `@ParameterObject RentalSearchCriteria` (no JPA entities in controller)
- [ ] `RentalRepositoryAdapter` converts criteria to Specification using mapper
- [ ] `FindRentalsService` simplified (no conditional logic, just passes criteria)
- [ ] All existing filter combinations work (status, customerId, status+customerId)
- [ ] All existing tests pass
- [ ] Unit tests for `RentalSpecificationMapper` added
- [ ] Component tests verify backward compatibility
- [ ] Documentation updated
- [ ] No breaking changes to API contract
- [ ] Code is cleaner and more maintainable
- [ ] Proper architectural layering (no JPA dependencies in web/domain layers)

## Expected Benefits

**Code Quality:**

- 📉 Reduced code complexity (eliminate conditional logic)
- 🔄 Less code duplication
- 📝 More declarative and readable code
- 🧪 Easier to test (test Specifications directly)

**Maintainability:**

- ➕ Easy to add new filters (just add `@Spec` annotation)
- 🔧 No need for new repository methods per filter combination
- 📚 Consistent with equipment filtering approach
- 🎯 Single source of truth for filtering logic

**Flexibility:**

- 🔍 Support for complex filters (ranges, LIKE, IN, OR)
- 🎨 Easy to combine multiple filters
- 🚀 Foundation for future advanced filtering needs

## Risks and Considerations

**Potential Issues:**

1. **SpecificationBuilder Implementation** - Need to verify if `SpecificationBuilder` utility exists or implement it
    - **Mitigation**: Check shared module, implement if needed following the pattern shown in example

2. **Type Conversion** - Need to handle enum/UUID to string conversion in mapper
    - **Mitigation**: Use `.map(Enum::name)` for enums, `.map(UUID::toString)` for UUIDs

3. **Performance** - Specifications might generate less optimal queries than hand-written methods
    - **Mitigation**: Review generated SQL, add indexes if needed, benchmark performance

4. **Learning Curve** - Team needs to understand `SpecificationBuilder` pattern
    - **Mitigation**: Good documentation, examples, code review

5. **Breaking Changes** - Risk of breaking existing API clients
    - **Mitigation**: Maintain backward compatibility, `@ParameterObject` preserves query parameter names

6. **Null Handling** - Need to handle null criteria gracefully
    - **Mitigation**: Use `Optional.ofNullable()` pattern, return empty Specification if criteria is null

**Testing Strategy:**

- Unit tests for Specification building
- Integration tests for repository adapter
- WebMvc tests for controller
- Component tests for full HTTP flow
- Performance tests for query generation

## Next Steps

1. Verify if `specification-arg-resolver` dependency is already present (from TECH-002)
2. Check if `SpecificationBuilder` utility exists in shared module, implement if needed
3. Create `RentalSearchCriteria` DTO
4. Create `RentalSpecificationMapper` with `SpecificationBuilder` pattern
5. Update repository adapter to use mapper
6. Update controller to use `@ParameterObject`
7. Update use case and service layers
8. Write tests for mapper and verify backward compatibility
9. Document changes and update progress
