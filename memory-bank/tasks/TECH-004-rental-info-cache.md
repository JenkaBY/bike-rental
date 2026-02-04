# [TECH-004] - Rental Information Cache in Finance Module

**Status:** Pending  
**Added:** 2026-02-04  
**Priority:** Medium  
**Module:** finance  
**Type:** Technical Debt / Architecture Improvement  
**Dependencies:** None (can be implemented independently)

## Problem Statement

The finance module's payment endpoints currently cannot distinguish between:

1. A rental that exists but has no payments
2. A rental that doesn't exist at all

When calling `GET /api/payments/by-rental/{rentalId}`, both scenarios return `200 OK` with an empty list, creating
ambiguity for API clients.

**Current behavior:**

```gherkin
Scenario: Get payments by rental id when no payments exist
When a GET request has been made to "/api/payments/by-rental/3001" endpoint
Then the response status is 200
And the response is empty list
```

**Issue**: We cannot tell if rental `3001` exists or not.

## Root Cause

The finance module (bounded context) doesn't have direct access to rental data. Payments reference `rentalId` as a
foreign key, but there's no local way to validate if that rental actually exists in the system.

## Proposed Solution

Implement an **event-driven rental information cache** within the finance bounded context:

### 1. Create `rental_infos` Table

A lightweight table to track rental existence and basic metadata:

```sql
CREATE TABLE rental_infos
(
    rental_id   BIGINT PRIMARY KEY,
    customer_id UUID        NOT NULL,
    status      VARCHAR(50) NOT NULL,
    created_at  TIMESTAMP   NOT NULL,
    updated_at  TIMESTAMP   NOT NULL,
    is_active   BOOLEAN     NOT NULL DEFAULT true
);

CREATE INDEX idx_rental_infos_customer_id ON rental_infos (customer_id);
CREATE INDEX idx_rental_infos_status ON rental_infos (status);
```

### 2. Domain Event Listeners

Subscribe to rental lifecycle events from the rental bounded context:

```java
// Domain Events (published by Rental module)
record RentalCreatedEvent(
                Long rentalId,
                UUID customerId,
                String status,
                Instant createdAt
        ) {
}

record RentalStatusChangedEvent(
        Long rentalId,
        String newStatus,
        Instant changedAt
) {
}

record RentalCompletedEvent(
        Long rentalId,
        Instant completedAt
) {
}
```

### 3. Event Handler Service

```java

@Service
public class RentalInfoEventHandler {

    private final RentalInfoRepository rentalInfoRepository;

    @EventListener
    @Transactional
    public void handleRentalCreated(RentalCreatedEvent event) {
        var rentalInfo = RentalInfo.builder()
                .rentalId(event.rentalId())
                .customerId(event.customerId())
                .status(event.status())
                .createdAt(event.createdAt())
                .updatedAt(event.createdAt())
                .isActive(true)
                .build();

        rentalInfoRepository.save(rentalInfo);
    }

    @EventListener
    @Transactional
    public void handleRentalStatusChanged(RentalStatusChangedEvent event) {
        rentalInfoRepository.findById(event.rentalId())
                .ifPresent(info -> {
                    info.setStatus(event.newStatus());
                    info.setUpdatedAt(event.changedAt());
                    rentalInfoRepository.save(info);
                });
    }

    @EventListener
    @Transactional
    public void handleRentalCompleted(RentalCompletedEvent event) {
        rentalInfoRepository.findById(event.rentalId())
                .ifPresent(info -> {
                    info.setIsActive(false);
                    info.setUpdatedAt(event.completedAt());
                    rentalInfoRepository.save(info);
                });
    }
}
```

### 4. Rental Info Service

```java

@Service
public class RentalInfoService {

    private final RentalInfoRepository rentalInfoRepository;

    public boolean exists(Long rentalId) {
        return rentalInfoRepository.existsById(rentalId);
    }

    public Optional<RentalInfo> findById(Long rentalId) {
        return rentalInfoRepository.findById(rentalId);
    }

    public boolean isActive(Long rentalId) {
        return rentalInfoRepository.findById(rentalId)
                .map(RentalInfo::isActive)
                .orElse(false);
    }
}
```

### 5. Updated Payment Query Controller

```java

@RestController
@RequestMapping("/api/payments")
public class PaymentQueryController {

    private final GetPaymentsByRentalIdUseCase getByRentalUseCase;
    private final RentalInfoService rentalInfoService;
    private final PaymentQueryMapper mapper;

    @GetMapping("/by-rental/{rentalId}")
    public ResponseEntity<List<PaymentResponse>> getByRental(@PathVariable Long rentalId) {
        // Validate rental exists
        if (!rentalInfoService.exists(rentalId)) {
            throw new RentalNotFoundException(rentalId);
        }

        var payments = getByRentalUseCase.execute(rentalId);
        var response = payments.stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }
}
```

## Implementation Plan

### Subtasks

| ID   | Description                                                                | Status      | Estimated Effort |
|------|----------------------------------------------------------------------------|-------------|------------------|
| 4.1  | Create database migration for `rental_infos` table                         | Not Started | 1h               |
| 4.2  | Create `RentalInfo` domain model and repository                            | Not Started | 2h               |
| 4.3  | Define domain events (RentalCreated, RentalStatusChanged, RentalCompleted) | Not Started | 1h               |
| 4.4  | Implement `RentalInfoEventHandler`                                         | Not Started | 3h               |
| 4.5  | Implement `RentalInfoService`                                              | Not Started | 2h               |
| 4.6  | Update `PaymentQueryController` to validate rental existence               | Not Started | 1h               |
| 4.7  | Create `RentalNotFoundException` and exception handler                     | Not Started | 1h               |
| 4.8  | Write unit tests for event handler and service                             | Not Started | 3h               |
| 4.9  | Update component tests to include rental validation scenarios              | Not Started | 2h               |
| 4.10 | Document event contract between Rental and Finance modules                 | Not Started | 1h               |

**Total Estimated Effort:** 17 hours (~2-3 days)

## Benefits

1. **API Clarity**: Distinguish between "no payments yet" (200 with empty list) and "rental not found" (404)
2. **Better UX**: Clients get meaningful error messages
3. **Decoupling**: No synchronous calls to rental service (eventual consistency via events)
4. **Performance**: Local cache lookup instead of cross-context queries
5. **Audit Trail**: Finance module maintains its own view of rental lifecycle
6. **Extensibility**: Can add rental metadata (customer ID, status) for future query optimizations

## Trade-offs

### Pros

- ✅ No cross-context synchronous coupling
- ✅ Fast local lookups
- ✅ Clear bounded context ownership
- ✅ Event-driven architecture

### Cons

- ❌ Eventual consistency (small lag between rental creation and cache update)
- ❌ Additional storage (minimal - just metadata)
- ❌ Event handling complexity
- ❌ Need to handle event replay/recovery scenarios

## Acceptance Criteria

- [ ] `rental_infos` table created with proper indexes
- [ ] Event handlers process RentalCreated, RentalStatusChanged, RentalCompleted events
- [ ] `RentalInfoService.exists(rentalId)` returns true/false based on cache
- [ ] `GET /api/payments/by-rental/{rentalId}` returns 404 for non-existent rentals
- [ ] `GET /api/payments/by-rental/{rentalId}` returns 200 + empty list for existing rentals with no payments
- [ ] Unit tests cover event handling logic
- [ ] Component tests validate both scenarios (rental exists/doesn't exist)
- [ ] Documentation describes event contract between modules

## Updated Component Test Scenarios

```gherkin
Scenario: Get payments by rental id when no payments exist
Given the following rental exists in the system
| rentalId | customerId | status |
| 3001     | CUST1      | ACTIVE |
And no payment records exist for rental 3001
When a GET request has been made to "/api/payments/by-rental/3001" endpoint
Then the response status is 200
And the response is empty list

Scenario: Get payments by non-existent rental id
When a GET request has been made to "/api/payments/by-rental/99999" endpoint
Then the response status is 404
And the response contains
| path     | value                                         |
| $.title  | Not Found                                     |
| $.detail | Rental with identifier '99999' not found      |
```

## Alternative Approaches Considered

### 1. Synchronous Call to Rental Service

```java
// Direct coupling - NOT RECOMMENDED
if(!rentalService.exists(rentalId)){
        throw new

RentalNotFoundException(rentalId);
}
```

**Rejected**: Creates tight coupling between bounded contexts, violates DDD principles.

### 2. Accept Current Limitation

**Rejected**: Poor API design, ambiguous responses harm developer experience.

### 3. Query Parameter Instead of Path Parameter

```java
GET /api/payments?rentalId=3001
```

**Rejected**: Changes API contract, doesn't solve the underlying problem.

## References

- Memory Bank Task: US-FN-001 (Payment Acceptance)
- Component Test: `component-test/src/test/resources/features/finance/payments.feature`
- Related Discussion: GET by rental ID endpoint behavior
- DDD Pattern: Bounded Context Event Subscription

## Notes

- This is a **non-breaking change** - existing API continues to work, behavior improves
- Can be implemented incrementally (table → event handlers → validation)
- Consider adding metrics for event processing lag
- May need event replay mechanism for historical rentals
- Rental module must publish events (coordinate with rental team)

## Risk Assessment

**Risk Level:** Low-Medium

**Risks:**

1. Event delivery failure → Rental not cached → False 404 errors
2. Event ordering issues → Stale status information
3. Database migration on production → Downtime considerations

**Mitigation:**

1. Implement event retry/dead letter queue
2. Use event timestamps and optimistic locking
3. Run migration during low-traffic window, add indexes after migration
