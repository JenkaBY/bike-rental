# [TECH-005] - Tariff Selection Cache

**Status:** Pending  
**Added:** 2026-02-09  
**Priority:** Medium  
**Module:** tariff  
**Type:** Performance Improvement  
**Dependencies:** US-RN-002 (can be implemented together or after)

## Problem Statement

The tariff selection process (`TariffFacade.selectTariff()`) is called frequently during rental creation and updates.
Each call performs:

1. Database query to fetch active tariffs for equipment type
2. Filtering by validity date
3. Period calculation and price comparison
4. Selection algorithm execution

**Current Performance Issues:**

- Database queries on every rental creation/update
- Repeated calculations for same equipment type + duration combinations
- No caching mechanism for frequently accessed tariff data
- Potential performance bottleneck as rental volume increases

**Use Cases:**

- Rental creation (`CreateRentalService`)
- Rental updates (`UpdateRentalService`) - when equipment or duration changes
- Tariff suggestion endpoint (`GET /api/tariffs/suggest`)

## Proposed Solution

Implement **Spring Cache** for tariff selection queries to reduce database load and improve response times.

### Architecture

- **Cache Name:** `tariff-selection`
- **Cache Key:** `{equipmentTypeSlug}_{durationMinutes}_{rentalDate}`
- **Cache TTL:** 5 minutes (configurable)
- **Cache Eviction:** On tariff activation/deactivation, tariff updates

### Implementation Details

1. **Spring Cache Setup**
    - Add `spring-boot-starter-cache` dependency
    - Configure cache manager (Caffeine or simple in-memory)
    - Create `CacheConfig` with cache definitions

2. **Cache Annotations**
    - `@Cacheable` on `TariffFacade.selectTariff()`
    - `@CacheEvict` on tariff activation/deactivation services
    - `@CacheEvict` on tariff update service

3. **Cache Configuration**
    - TTL: 5 minutes (tariffs don't change frequently)
    - Max size: 1000 entries (reasonable for typical equipment types)
    - Eviction policy: LRU (Least Recently Used)

## Implementation Plan

### Subtasks

| ID   | Description                                                | Status      | Estimated Effort |
|------|------------------------------------------------------------|-------------|------------------|
| 5.1  | Add `spring-boot-starter-cache` dependency to build.gradle | Not Started | 15 min           |
| 5.2  | Create `CacheConfig` with `tariff-selection` cache         | Not Started | 1h               |
| 5.3  | Add `@EnableCaching` to application configuration          | Not Started | 15 min           |
| 5.4  | Add `@Cacheable` to `TariffFacade.selectTariff()`          | Not Started | 30 min           |
| 5.5  | Add `@CacheEvict` to `ActivateTariffService`               | Not Started | 15 min           |
| 5.6  | Add `@CacheEvict` to `DeactivateTariffService`             | Not Started | 15 min           |
| 5.7  | Add `@CacheEvict` to `UpdateTariffService`                 | Not Started | 15 min           |
| 5.8  | Configure cache properties in `application.yaml`           | Not Started | 30 min           |
| 5.9  | Write unit tests for cache behavior                        | Not Started | 2h               |
| 5.10 | Write integration tests (verify cache hits/misses)         | Not Started | 2h               |
| 5.11 | Performance testing (benchmark with/without cache)         | Not Started | 2h               |

**Total Estimated Effort:** ~10 hours (~1.5 days)

## Technical Details

### Cache Configuration

**File:** `service/src/main/java/com/github/jenkaby/bikerental/shared/config/CacheConfig.java`

```java

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("tariff-selection");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats());
        return cacheManager;
    }
}
```

### Cache Usage

**File:** `service/src/main/java/com/github/jenkaby/bikerental/tariff/TariffFacadeImpl.java`

```java

@Override
@Cacheable(
        value = "tariff-selection",
        key = "#equipmentTypeSlug + '_' + #duration.toMinutes() + '_' + #rentalDate"
)
public TariffInfo selectTariff(String equipmentTypeSlug, Duration duration, LocalDate rentalDate) {
    // Existing implementation
}
```

### Cache Eviction

**File:** `service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/ActivateTariffService.java`

```java

@Override
@CacheEvict(value = "tariff-selection", allEntries = true)
@Transactional
public Tariff execute(Long tariffId) {
    // Existing implementation
}
```

**File:** `service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/DeactivateTariffService.java`

```java

@Override
@CacheEvict(value = "tariff-selection", allEntries = true)
@Transactional
public Tariff execute(Long tariffId) {
    // Existing implementation
}
```

**File:** `service/src/main/java/com/github/jenkaby/bikerental/tariff/application/service/UpdateTariffService.java`

```java

@Override
@CacheEvict(value = "tariff-selection", allEntries = true)
@Transactional
public Tariff execute(UpdateTariffCommand command) {
    // Existing implementation
}
```

### Application Configuration

**File:** `service/src/main/resources/application.yaml`

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m
```

## Benefits

1. **Performance**: Reduced database queries for repeated tariff selections
2. **Scalability**: Better handling of high rental creation volume
3. **Response Time**: Faster tariff selection (cache hit vs database query)
4. **Database Load**: Reduced load on tariff queries
5. **Cost**: Lower database resource usage

## Trade-offs

### Pros

- ✅ Significant performance improvement for repeated queries
- ✅ Simple implementation with Spring Cache abstraction
- ✅ Automatic cache invalidation on tariff changes
- ✅ Configurable TTL and size limits
- ✅ Cache statistics for monitoring

### Cons

- ❌ Memory usage (stored in application memory)
- ❌ Stale data risk if cache eviction fails (mitigated by TTL)
- ❌ Cache invalidation on all entries (could be optimized to specific keys)
- ❌ Not distributed (single instance cache)

## Cache Key Strategy

**Current:** `{equipmentTypeSlug}_{durationMinutes}_{rentalDate}`

**Example Keys:**

- `bike_60_2026-02-10`
- `scooter_120_2026-02-10`
- `bike_1440_2026-02-11`

**Considerations:**

- Includes all relevant parameters for accurate selection
- Date-based ensures validity period checks remain correct
- Duration in minutes provides granularity

**Future Optimization:**

- Could use more specific cache eviction (by equipment type) instead of `allEntries = true`
- Would require custom cache key generation and eviction logic

## Testing Strategy

### Unit Tests

- Verify `@Cacheable` annotation works correctly
- Verify cache key generation
- Verify cache eviction on tariff changes
- Mock cache manager for testing

### Integration Tests

- Verify cache hits on repeated calls with same parameters
- Verify cache misses on different parameters
- Verify cache eviction after tariff update
- Verify TTL expiration

### Performance Tests

- Benchmark tariff selection with cache vs without cache
- Measure cache hit ratio under typical load
- Measure memory usage with cache
- Load testing with concurrent requests

## Acceptance Criteria

- [ ] Spring Cache dependency added
- [ ] `CacheConfig` created with `tariff-selection` cache
- [ ] `@Cacheable` added to `TariffFacade.selectTariff()`
- [ ] `@CacheEvict` added to tariff activation/deactivation/update services
- [ ] Cache configuration in `application.yaml`
- [ ] Unit tests verify cache behavior
- [ ] Integration tests verify cache hits/misses
- [ ] Performance improvement measured (target: 50%+ reduction in DB queries for repeated selections)
- [ ] Cache statistics available for monitoring
- [ ] Documentation updated

## Monitoring

**Cache Metrics to Track:**

- Cache hit ratio (target: >70% for typical usage)
- Cache miss count
- Cache eviction count
- Average response time (cached vs non-cached)
- Memory usage

**Spring Boot Actuator:**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: cache,metrics
  metrics:
    export:
      prometheus:
        enabled: true
```

## Risks and Mitigation

**Risk 1: Stale Data**

- **Mitigation:** TTL of 5 minutes ensures fresh data, cache eviction on tariff changes

**Risk 2: Memory Usage**

- **Mitigation:** Maximum size limit (1000 entries), LRU eviction policy

**Risk 3: Cache Invalidation Failure**

- **Mitigation:** TTL ensures eventual consistency, monitoring cache statistics

**Risk 4: Distributed Cache Needed**

- **Mitigation:** Current solution is single-instance. If scaling to multiple instances, consider Redis cache

## Future Enhancements

1. **Distributed Cache**: Migrate to Redis if multiple application instances
2. **Selective Eviction**: Evict only affected cache keys instead of all entries
3. **Cache Warming**: Pre-populate cache with common equipment types
4. **Cache Statistics Dashboard**: Monitor cache performance in admin UI

## References

- Related Task: US-RN-002 (Automatic Tariff Selection)
- Spring Cache Documentation: https://docs.spring.io/spring-framework/reference/integration/cache.html
- Caffeine Cache: https://github.com/ben-manes/caffeine

## Notes

- Can be implemented independently or together with US-RN-002
- Non-breaking change - existing functionality remains unchanged
- Cache is transparent to calling code
- Consider implementing after US-RN-002 to measure baseline performance first
