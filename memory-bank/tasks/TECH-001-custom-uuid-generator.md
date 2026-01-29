# [TECH-001] - Custom UUID Generator for Hibernate Entity IDs

**Status:** Pending  
**Added:** 2026-01-29  
**Updated:** 2026-01-29  
**Priority:** Medium  
**Module:** shared (affects all modules)  
**Type:** Technical Improvement  
**Dependencies:** None

## Original Request

Implement a custom UUID generator for Hibernate entity IDs to replace the default UUID generation strategy with a more
performant and database-friendly approach.

## Problem Statement

**Current Situation:**

- JPA entities currently use `@GeneratedValue(strategy = GenerationType.AUTO)` or default UUID generation
- Default UUID generation in Hibernate uses random UUIDs (UUID v4)
- Random UUIDs can cause performance issues in database indexes due to their random nature
- No control over UUID generation strategy

**Issues with Random UUIDs:**

1. **Index Fragmentation** - Random UUIDs cause B-tree index fragmentation in PostgreSQL
2. **Performance Degradation** - Insert performance degrades as table grows due to random nature
3. **Storage Overhead** - Poor locality of reference leads to more disk I/O
4. **Cache Inefficiency** - Random distribution reduces database cache effectiveness

## Solution Approach

Implement a custom UUID generator using **UUID v7** (time-ordered) or **ULID** (Universally Unique Lexicographically
Sortable Identifier).

**Benefits:**

- ✅ Time-ordered UUIDs maintain insert performance
- ✅ Better database index locality
- ✅ Improved cache efficiency
- ✅ Maintains global uniqueness
- ✅ Still compatible with UUID columns
- ✅ Sortable by creation time

**Recommended Approach: UUID v7**

- RFC 9562 standard (timestamp-based UUID)
- First 48 bits: Unix timestamp in milliseconds
- Remaining bits: random data for uniqueness
- Lexicographically sortable
- Better database performance than UUID v4

## Implementation Plan

### Phase 1: Research and Setup

- [ ] Research UUID v7 libraries for Java (e.g., `java-uuid-generator`, `uuid-creator`)
- [ ] Evaluate ULID as alternative (e.g., `ulid-creator` library)
- [ ] Choose generation strategy (UUID v7 recommended)
- [ ] Add dependency to Gradle

### Phase 2: Implementation

- [ ] Create `CustomUuidGenerator` class implementing Hibernate `IdentifierGenerator`
- [ ] Implement UUID v7 generation logic
- [ ] Add configuration for generator
- [ ] Create custom annotation `@UuidV7` for easy usage
- [ ] Document generator behavior and benefits

### Phase 3: Integration

- [ ] Update `Customer` entity to use custom generator
- [ ] Update `CustomerJpaEntity` to use custom generator
- [ ] Create database migration to handle existing data (if needed)
- [ ] Update other entities as they are created

### Phase 4: Testing

- [ ] Write unit tests for UUID generator
    - Verify format (UUID v7 structure)
    - Verify uniqueness
    - Verify sortability (lexicographic order matches temporal order)
    - Verify performance (benchmark vs default)
- [ ] Write integration tests with JPA
    - Test entity persistence
    - Test ID generation on save
    - Verify database compatibility
- [ ] Performance testing
    - Benchmark insert performance
    - Compare with random UUID
    - Measure index fragmentation

### Phase 5: Documentation

- [ ] Document why UUID v7 was chosen
- [ ] Add usage guide for developers
- [ ] Update architecture documentation
- [ ] Add performance benchmarks to docs

## Technical Details

### Recommended Library

**Option 1: uuid-creator** (Recommended)

```gradle
implementation 'com.github.f4b6a3:uuid-creator:5.3.7'
```

**Option 2: java-uuid-generator**

```gradle
implementation 'com.fasterxml.uuid:java-uuid-generator:4.3.0'
```

### Implementation Example

```java
package com.github.jenkaby.bikerental.shared.persistence;

import com.github.f4b6a3.uuid.UuidCreator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.util.UUID;

public class UuidV7Generator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        // Generate time-ordered UUID v7
        return UuidCreator.getTimeOrderedEpoch();
    }
}
```

### Custom Annotation

```java
package com.github.jenkaby.bikerental.shared.persistence;

import org.hibernate.annotations.GenericGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@GenericGenerator(
        name = "uuid-v7",
        strategy = "com.github.jenkaby.bikerental.shared.persistence.UuidV7Generator"
)
public @interface UuidV7 {
}
```

### Entity Usage

```java

@Entity
@Table(name = "customers")
public class CustomerJpaEntity {

    @Id
    @GeneratedValue(generator = "uuid-v7")
    @GenericGenerator(
            name = "uuid-v7",
            strategy = "com.github.jenkaby.bikerental.shared.persistence.UuidV7Generator"
    )
    private UUID id;

    // ... other fields
}
```

### Alternative: Using @IdGeneratorType (Hibernate 6.2+)

```java

@IdGeneratorType(UuidV7Generator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UuidV7 {
}

// Usage in entity
@Entity
public class CustomerJpaEntity {
    @Id
    @UuidV7
    private UUID id;
}
```

## Package Structure

```
com.github.jenkaby.bikerental.shared
└── persistence
    ├── UuidV7Generator.java (Hibernate generator implementation)
    └── UuidV7.java (Optional: custom annotation)
```

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                         | Status      | Updated    | Notes                        |
|-----|-------------------------------------|-------------|------------|------------------------------|
| 1.1 | Research UUID v7 libraries          | Not Started | 2026-01-29 | Evaluate uuid-creator        |
| 1.2 | Add dependency to Gradle            | Not Started | 2026-01-29 |                              |
| 1.3 | Implement UuidV7Generator class     | Not Started | 2026-01-29 | Core generator logic         |
| 1.4 | Create custom annotation (optional) | Not Started | 2026-01-29 | @UuidV7 for easier usage     |
| 1.5 | Update Customer entities            | Not Started | 2026-01-29 | Apply to existing entities   |
| 1.6 | Write unit tests                    | Not Started | 2026-01-29 | Format, uniqueness, sortable |
| 1.7 | Write integration tests             | Not Started | 2026-01-29 | JPA persistence tests        |
| 1.8 | Performance benchmarking            | Not Started | 2026-01-29 | Compare with UUID v4         |
| 1.9 | Update documentation                | Not Started | 2026-01-29 | Architecture docs            |

## Progress Log

### 2026-01-29

- Task created
- Documented problem with random UUIDs (index fragmentation, performance)
- Recommended UUID v7 as solution
- Outlined implementation approach with uuid-creator library
- Defined 9 subtasks for implementation
- Status: Ready to start

## Expected Benefits

**Performance Improvements:**

- ⚡ 20-40% faster inserts in large tables (typical improvement)
- 📊 Reduced index fragmentation
- 💾 Better database cache utilization
- 🔍 Faster range queries due to time-ordering

**Developer Experience:**

- 🎯 Simple annotation-based usage
- 📝 Clear documentation
- 🔄 Easy to apply to new entities
- ✅ No code changes in business logic

**Database Benefits:**

- 📈 Linear index growth pattern
- 🗜️ Better compression (sequential data)
- 🚀 Improved VACUUM performance (PostgreSQL)
- 💿 Reduced disk I/O

## Risks and Considerations

**Potential Issues:**

1. **Clock Skew** - If system clock goes backward, could generate duplicate UUIDs
    - Mitigation: uuid-creator library handles this with monotonic counter

2. **Timestamp Precision** - Limited to millisecond precision
    - Mitigation: Random bits provide uniqueness within same millisecond

3. **Compatibility** - Ensure UUID v7 is compatible with PostgreSQL UUID type
    - Mitigation: UUID v7 is still a valid RFC 4122 UUID, fully compatible

4. **Migration** - Existing entities with UUID v4 IDs
    - Mitigation: No migration needed, only affects new records

**Testing Strategy:**

- Unit tests for generator logic
- Integration tests with actual database
- Performance benchmarks with metrics
- Load testing with high insert volume

## Acceptance Criteria

- [ ] Custom UUID generator implemented and working
- [ ] All new entities use UUID v7 generator
- [ ] Existing Customer entity updated (for new records)
- [ ] Unit tests covering format, uniqueness, sortability
- [ ] Integration tests with JPA passing
- [ ] Performance benchmarks showing improvement over UUID v4
- [ ] Documentation updated with usage guide
- [ ] Zero breaking changes to existing functionality

## References

**Standards:**

- [RFC 9562: UUID Version 7](https://www.rfc-editor.org/rfc/rfc9562.html#name-uuid-version-7)
- [UUID Best Practices](https://vladmihalcea.com/uuid-database-primary-key/)

**Libraries:**

- [uuid-creator on GitHub](https://github.com/f4b6a3/uuid-creator)
- [ULID Specification](https://github.com/ulid/spec)

**Articles:**

- [PostgreSQL UUID Performance](https://www.2ndquadrant.com/en/blog/sequential-uuid-generators/)
- [UUID v7 vs v4 Comparison](https://blog.daveallie.com/ulid-primary-keys)

**Related Tasks:**

- US-CL-002: Quick Customer Creation (already implemented with default UUID)
- US-CL-003: Full Customer Profile Management (already implemented)
- Future tasks: All entities with UUID primary keys

## Known Issues

None yet - task not started

## Notes

- This is a non-breaking change - existing UUIDs remain unchanged
- Only affects new entities created after implementation
- Can be rolled out incrementally per entity
- Consider making this part of entity base class or archetype
- Should be applied to all future entities from the start
