# [US-AD-003] - Настройка тарифов (Tariff Configuration)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** High  
**Module:** admin  
**Dependencies:** US-TR-001

## Original Request

**Как** Администратор  
**Я хочу** создавать и изменять тарифы  
**Чтобы** управлять ценообразованием

## User Story Details

**Описание:**  
Система должна позволять администратору создавать и изменять тарифы.

**Функции:**

- Создание нового тарифа
- Редактирование существующего
- Копирование тарифа
- Активация/деактивация
- Установка периода действия

**Критерии приемки:**

- Удобный интерфейс управления тарифами
- Валидация (цена > 0, корректные даты)
- Невозможность удалить тариф, используемый в активных арендах
- История изменений тарифов

**Связанные требования:** FR-AD-003

## Thought Process

This user story provides administrative UI/API for managing the tariff catalog created in US-TR-001. Key considerations:

1. **Admin-Only Operations**: Only administrators can modify tariffs
2. **Business Continuity**: Cannot delete tariffs used in active rentals
3. **Audit Trail**: Track all tariff changes for compliance
4. **Copy Functionality**: Clone existing tariffs to create variants
5. **Soft Delete**: Consider archiving instead of deleting

**Technical Approach:**

- Admin endpoints for CRUD operations on tariffs
- Special handling for tariffs in use (prevent deletion)
- Tariff copy creates new tariff with same settings
- Audit trail using events or database triggers
- Versioning support (new tariff version vs editing existing)

**Business Rules:**

- Price must be positive
- ValidFrom < ValidTo (if ValidTo is set)
- Cannot delete tariff if referenced by any rental (completed or active)
- Deactivation is always allowed (prevents new rentals, doesn't affect existing)

## Implementation Plan

- [ ] Extend tariff endpoints with admin operations
- [ ] Implement copy tariff functionality
- [ ] Add tariff deletion with active rental check
- [ ] Implement activation/deactivation
- [ ] Add tariff audit trail
- [ ] Create validation rules
- [ ] Implement query to check tariff usage in rentals
- [ ] Create component tests for all operations
- [ ] Write unit tests for validation
- [ ] Write WebMvc tests with admin role
- [ ] Document tariff management workflows

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description                        | Status      | Updated    | Notes |
|-----|------------------------------------|-------------|------------|-------|
| 3.1 | Extend tariff management endpoints | Not Started | 2026-01-26 |       |
| 3.2 | Implement copy tariff              | Not Started | 2026-01-26 |       |
| 3.3 | Add deletion protection            | Not Started | 2026-01-26 |       |
| 3.4 | Implement audit trail              | Not Started | 2026-01-26 |       |
| 3.5 | Add validation rules               | Not Started | 2026-01-26 |       |
| 3.6 | Create tests                       | Not Started | 2026-01-26 |       |

## Progress Log

### 2026-01-26

- Task created in Memory Bank structure
- Status: Pending, depends on US-TR-001 completion
- Part of Phase 2: Basic Module Functions

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.admin
├── web.command
│   ├── TariffAdminController
│   ├── dto.CopyTariffRequest
│   └── dto.TariffManagementResponse
├── application
│   ├── usecase.CopyTariffUseCase
│   ├── usecase.DeleteTariffUseCase
│   └── service.TariffManagementService
└── domain
    └── validation.TariffDeletionValidator
```

**API Endpoints:**

- `POST /api/admin/tariffs/{id}/copy` - Copy existing tariff (ADMIN only)
- `DELETE /api/admin/tariffs/{id}` - Delete tariff (ADMIN only, with checks)
- `GET /api/admin/tariffs/{id}/usage` - Check if tariff is in use
- `GET /api/admin/tariffs/{id}/history` - Get tariff change history

All existing tariff CRUD from US-TR-001 extended with admin security.

**Copy Tariff Request:**

```json
{
  "newName": "Weekend Special - Copy",
  "validFrom": "2026-06-01",
  "adjustPricePercent": 10
  // Optional: increase/decrease by percentage
}
```

**Tariff Usage Response:**

```json
{
  "tariffId": "uuid",
  "tariffName": "Hourly Rate",
  "isInUse": true,
  "activeRentalsCount": 5,
  "totalRentalsCount": 150,
  "canDelete": false
}
```

**Deletion Protection:**

```java

@Service
public class TariffDeletionValidator {

    public void validateDeletion(UUID tariffId) {
        long activeRentals = rentalRepository.countByTariffIdAndStatusIn(
                tariffId,
                List.of(RentalStatus.DRAFT, RentalStatus.ACTIVE)
        );

        if (activeRentals > 0) {
            throw new TariffInUseException(
                    "Cannot delete tariff. " + activeRentals + " active rentals are using it."
            );
        }

        // Optional: Also check historical rentals
        long historicalRentals = rentalRepository.countByTariffId(tariffId);
        if (historicalRentals > 0) {
            throw new TariffInUseException(
                    "Cannot delete tariff. " + historicalRentals + " rentals exist in history."
            );
        }
    }
}
```

**Copy Tariff Logic:**

```java
public Tariff copyTariff(UUID sourceTariffId, CopyTariffCommand command) {
    Tariff source = tariffRepository.findById(sourceTariffId)
            .orElseThrow(() -> new TariffNotFoundException(sourceTariffId));

    Money newBasePrice = command.adjustPricePercent() != null
            ? source.getBasePrice().adjustByPercent(command.adjustPricePercent())
            : source.getBasePrice();

    Tariff copy = Tariff.builder()
            .name(command.newName())
            .equipmentTypeId(source.getEquipmentTypeId())
            .period(source.getPeriod())
            .basePrice(newBasePrice)
            .extraTimePricePer5Min(source.getExtraTimePricePer5Min())
            .validFrom(command.validFrom())
            .validTo(command.validTo())
            .isActive(false)  // Copies start as inactive
            .build();

    return tariffRepository.save(copy);
}
```

**Audit Trail Options:**

1. **Domain Events**: Publish TariffCreated, TariffUpdated, TariffDeleted events
2. **Spring Data Envers**: Automatic entity versioning
3. **Custom Audit Table**: Dedicated tariff_audit_log table

**Validation Rules:**

```java
public class TariffValidator {
    public void validate(Tariff tariff) {
        if (tariff.getBasePrice().isNegativeOrZero()) {
            throw new ValidationException("Base price must be positive");
        }
        if (tariff.getExtraTimePricePer5Min().isNegative()) {
            throw new ValidationException("Extra time price cannot be negative");
        }
        if (tariff.getValidTo() != null &&
                tariff.getValidFrom().isAfter(tariff.getValidTo())) {
            throw new ValidationException("ValidFrom must be before ValidTo");
        }
    }
}
```

## Known Issues

None yet - task not started

## References

- User Story File: [docs/tasks/us/US-AD-003/us-ad-003.md](../../../docs/tasks/us/US-AD-003/us-ad-003.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Dependency: US-TR-001 (Tariff catalog - must be complete)
- Related: Tariff management affects rental pricing across the system
