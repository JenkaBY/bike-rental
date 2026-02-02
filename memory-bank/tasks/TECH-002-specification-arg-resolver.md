# [TECH-002] Integrate specification-arg-resolver for equipment filtering

**Status:** Pending  
**Added:** 2026-02-02  
**Updated:** 2026-02-02

## Original Request

Add support for advanced filtering on the equipment search endpoint by integrating the
`net.kaczmarzyk:specification-arg-resolver` library so clients can use expressive query parameters (e.g., equals, like,
ranges) and keep backward compatibility with existing query params (status, type, serial, uid).

## Goal

Provide a robust, maintainable way to express filtering for equipment search using Specifications resolved from HTTP
query parameters. Preserve current API behavior for existing clients and add tests and documentation.

## Thought Process

Using `specification-arg-resolver` reduces custom parsing code and makes it simple to support complex filters (AND/OR,
range, like). We'll add the library dependency, enable Specification use in the JPA repository, adapt the controller to
accept resolved Specification parameters and Pageable, and route the Specification to the repository adapter which will
call `findAll(spec, pageable)`. To avoid breaking clients, legacy request params will be converted into equivalent
Specifications and composed with the one resolved by the library.

## Files & Components Likely to Change

- `service/build.gradle` (add dependency)
- `gradle/libs.versions.toml` (optional libs alias)
- `service/src/main/java/.../equipment/web/query/controller/EquipmentQueryController.java` (accept Specification/Specs)
- `service/src/main/java/.../equipment/infrastructure/persistence/repository/EquipmentJpaRepository.java` (extend
  JpaSpecificationExecutor)
- `service/src/main/java/.../equipment/infrastructure/persistence/adapter/EquipmentRepositoryAdapter.java` (use
  jpaRepository.findAll(spec, pageable))
- `service/src/main/java/.../equipment/web/query/mapper/EquipmentQueryMapper.java` (compat layer: params ->
  Specification)
- Tests: component-test module and service unit tests

## Gradle Dependency

Add to `gradle/libs.versions.toml` and then to `service/build.gradle`:

implementation "net.kaczmarzyk:specification-arg-resolver:{latest version supporting SpringBoot4+}"

(If the project uses catalog aliases, add a libs entry instead.)

## Implementation Plan (concrete steps)

1. Add dependency to `service/build.gradle` (and libs catalog if used).
2. Update `EquipmentJpaRepository` to extend `JpaSpecificationExecutor<EquipmentJpaEntity>`.
3. Update `EquipmentQueryController.searchEquipments(...)` to accept a `Specification<EquipmentJpaEntity>` produced by
   `@Spec`/`@And` annotations (or multiple @Spec arguments) and `Pageable`.
4. In `EquipmentRepositoryAdapter`, add overload/branch that takes a Specification and calls
   `jpaRepository.findAll(spec, pageRequest)`, mapping results to domain DTOs via existing mappers.
5. Implement a small compatibility helper in `EquipmentQueryMapper` (or controller) to convert old `@RequestParam`
   values to a Specification and combine with the resolved spec:
   `finalSpec = Specification.where(legacySpec).and(resolverSpec)`.
6. Add unit tests for adapter/service to verify `findAll(spec, pageable)` is called and mapping is correct.
7. Add component tests (in `component-test`) covering the endpoint with example query params: single filters, combined
   filters, and legacy params.
8. Update documentation (API docs and memory bank progress) and run full build/tests.

## Example Code Snippets (reference)

- Repository:
  public interface EquipmentJpaRepository extends JpaRepository<EquipmentJpaEntity, Long>,
  JpaSpecificationExecutor<EquipmentJpaEntity> { /* existing methods */ }

- Controller (signature idea):
  @GetMapping
  public ResponseEntity<Page<EquipmentResponse>> searchEquipments(
  @PageableDefault(size = 20, sort = "serialNumber") Pageable pageable,
  @And({
  @Spec(path = "statusSlug", params = "status", spec = Equal.class),
  @Spec(path = "equipmentTypeSlug", params = "type", spec = Equal.class),
  @Spec(path = "serialNumber", params = "serial", spec = Like.class),
  @Spec(path = "uid", params = "uid", spec = Equal.class)
  }) Specification<EquipmentJpaEntity> spec) { ... }

- Adapter usage:
  var page = equipmentJpaRepository.findAll(spec, pageRequest);

## Tests

- Component tests: verify HTTP filtering behavior for status/type/serial/uid and combinations. Use existing
  component-test fixtures.
- Unit tests: mock jpa repository and verify `findAll(spec, pageable)` is invoked and mapping is correct.

## Migration & Rollback

- Keep legacy params supported by converting them to Specifications and composing with resolver spec.
- To rollback: remove dependency and revert controller/adapter changes.

## Estimated Effort

~2 days (one engineer) including tests and docs.

## Subtasks (checklist)

- [ ] Add dependency to `service/build.gradle` (+ libs catalog)
- [ ] Extend `EquipmentJpaRepository` with `JpaSpecificationExecutor`
- [ ] Update `EquipmentQueryController` to accept `Specification`/@Spec
- [ ] Update `EquipmentRepositoryAdapter` to support Specification-based queries
- [ ] Implement compatibility conversion for legacy params
- [ ] Add unit tests for adapter/service
- [ ] Add component tests for endpoint
- [ ] Update memory-bank progress and task index

## Notes / Gotchas

- Handler argument resolver should register automatically when the library is present; if not, register
  `SpecificationArgumentResolver` in WebMvc config.
- Make sure @Spec `path` values match JPA entity field names (not DTO names).
- Avoid parameter name collisions with Pageable params (`page`, `size`, `sort`).
- Restrict allowed filterable fields to avoid exposing sensitive properties.

## Next Steps

If you confirm the dependency version (4.0.0 is recommended for Spring Boot 4) and whether to enable immediately or
behind a feature flag, I will implement the first code changes and tests. 
