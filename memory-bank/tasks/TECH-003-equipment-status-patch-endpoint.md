# [TECH-003] - Add PATCH Endpoint for Equipment Status Change

**Status:** Pending  
**Added:** 2026-02-02  
**Updated:** 2026-02-02  
**Priority:** Medium  
**Module:** Equipment  
**Type:** Technical Debt / Enhancement

## Original Request

Create a PATCH endpoint to cover changing status of equipment. Currently, the system requires a full PUT update to
change equipment status, which is not RESTful for partial updates.

## Problem Statement

The current implementation only supports:

- POST `/api/equipments` - Create new equipment (full data)
- PUT `/api/equipments/{id}` - Update equipment (full data replacement)
- GET endpoints for querying equipment

**Missing:** A PATCH endpoint specifically for status transitions, which is a common operation that should be:

1. Atomic - only changing the status field
2. RESTful - using PATCH for partial updates
3. Validated - ensuring valid status transitions (e.g., available → rented → maintenance)
4. Auditable - tracking status changes for equipment lifecycle

## Thought Process

### Why PATCH Instead of PUT?

- **PUT** semantics: Replace entire resource (requires all fields)
- **PATCH** semantics: Partial update (only status field changes)
- Status changes are frequent operations that shouldn't require sending all equipment data
- Follows RESTful best practices for partial resource updates

### Design Considerations

1. **Endpoint Design**:
    - `PATCH /api/equipments/{id}` - Specific status change endpoint
    - Request body: `{ "status": "rented" }` or `{ "statusSlug": "rented" }`

2. **Validation**:
    - Validate status slug exists in equipment_statuses table
    - Optional: Add state machine validation for valid transitions
    - Return 400 for invalid status slugs
    - Return 404 if equipment not found

3. **Use Case Layer**:
    - Create `ChangeEquipmentStatusUseCase` interface
    - Implement `ChangeEquipmentStatusService`
    - Command: `ChangeEquipmentStatusCommand(equipmentId, newStatusSlug)`

4. **Domain Layer**:
    - Add `changeStatus(EquipmentStatus newStatus)` method to Equipment entity
    - Optional: Implement state transition validation in domain

5. **Testing**:
    - Unit tests for service (valid/invalid status changes)
    - WebMvc tests for controller (201 success, 400 validation, 404 not found)
    - Component tests for end-to-end flow

## Implementation Plan

### Phase 1: Domain & Application Layer

- [ ] 1.1 Add `changeStatus()` method to Equipment domain entity
- [ ] 1.2 Create `ChangeEquipmentStatusUseCase` interface in application layer
- [ ] 1.3 Create `ChangeEquipmentStatusCommand` record
- [ ] 1.4 Implement `ChangeEquipmentStatusService`
- [ ] 1.5 Write unit tests for service (TDD approach)

### Phase 2: Web Layer

- [ ] 2.1 Create `EquipmentStatusChangeRequest` DTO with validation
- [ ] 2.2 Add mapper method `toChangeStatusCommand()`
- [ ] 2.3 Add PATCH endpoint to `EquipmentCommandController`
- [ ] 2.4 Write WebMvc tests following java-junit.prompt.md guidelines

### Phase 3: Integration & Documentation

- [ ] 3.1 Write component tests for PATCH endpoint
- [ ] 3.2 Update API documentation (if exists)
- [ ] 3.3 Verify architecture compliance (no Spring in domain)
- [ ] 3.4 Run full test suite

## Progress Tracking

**Overall Status:** Not Started - 0% Complete

### Subtasks

| ID  | Description                                   | Status      | Updated    | Notes                             |
|-----|-----------------------------------------------|-------------|------------|-----------------------------------|
| 1.1 | Add changeStatus() to Equipment entity        | Not Started | 2026-02-02 | Domain layer method               |
| 1.2 | Create ChangeEquipmentStatusUseCase interface | Not Started | 2026-02-02 | Application port                  |
| 1.3 | Create ChangeEquipmentStatusCommand record    | Not Started | 2026-02-02 | Command object                    |
| 1.4 | Implement ChangeEquipmentStatusService        | Not Started | 2026-02-02 | Service implementation            |
| 1.5 | Unit tests for service                        | Not Started | 2026-02-02 | TDD approach                      |
| 2.1 | Create EquipmentStatusChangeRequest DTO       | Not Started | 2026-02-02 | With @Slug validation             |
| 2.2 | Add mapper method                             | Not Started | 2026-02-02 | MapStruct mapper                  |
| 2.3 | PATCH endpoint in controller                  | Not Started | 2026-02-02 | PATCH /api/equipments/{id}/status |
| 2.4 | WebMvc tests for PATCH                        | Not Started | 2026-02-02 | Following project guidelines      |
| 3.1 | Component tests                               | Not Started | 2026-02-02 | End-to-end testing                |
| 3.2 | Update documentation                          | Not Started | 2026-02-02 | API docs                          |
| 3.3 | Architecture verification                     | Not Started | 2026-02-02 | No violations                     |
| 3.4 | Full test suite run                           | Not Started | 2026-02-02 | Regression testing                |

## Technical Decisions

### Request DTO Structure

```java
public record EquipmentStatusChangeRequest(
        @Slug
        @NotBlank(message = "Status is required")
        String statusSlug
) {
}
```

### Controller Method Signature

```java

@PatchMapping("/{id}/status")
public ResponseEntity<EquipmentResponse> changeStatus(
        @PathVariable("id") Long id,
        @Valid @RequestBody EquipmentStatusChangeRequest request
) {
    // Implementation
}
```

### Use Case Interface

```java
public interface ChangeEquipmentStatusUseCase {
    Equipment execute(ChangeEquipmentStatusCommand command);

    record ChangeEquipmentStatusCommand(
            Long equipmentId,
            String statusSlug
    ) {
    }
}
```

## Dependencies

- Depends on: US-EQ-001 (Equipment Catalog) - equipment entities and statuses must exist
- Blocks: US-EQ-004 (Equipment Status Management) - provides foundation for status workflows

## Acceptance Criteria

- [ ] PATCH endpoint responds with 200 OK on successful status change
- [ ] Returns updated equipment with new status
- [ ] Returns 400 Bad Request for invalid status slug
- [ ] Returns 404 Not Found if equipment doesn't exist
- [ ] Validates status slug using @Slug annotation
- [ ] All tests pass (unit, WebMvc, component)
- [ ] No architecture violations (domain layer clean)
- [ ] Follows project coding standards and guidelines

## Progress Log

### 2026-02-02

- Task created based on user request
- Defined implementation plan with 3 phases
- Outlined technical decisions and acceptance criteria
- Status: Pending, awaiting implementation start
