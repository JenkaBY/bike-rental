# [US-EQ-003] - Сканирование NFC-метки при возврате (NFC Tag Scanning on Return)

**Status:** Pending  
**Added:** 2026-01-26  
**Updated:** 2026-01-26  
**Priority:** High  
**Module:** equipment  
**Dependencies:** US-EQ-001, US-RN-005

## Original Request

**Как** Оператор проката  
**Я хочу** сканировать NFC-метку оборудования при возврате  
**Чтобы** автоматически идентифицировать возвращаемое оборудование

## User Story Details

**Описание:**  
Система должна поддерживать считывание NFC-меток оборудования через мобильное устройство при возврате.

**Критерии приемки:**

- Поддержка камеры мобильного устройства
- Автоматическое сопоставление UID с оборудованием
- Пометка оборудования как возвращенного
- Обработка ошибок (метка не найдена, оборудование не в аренде)

**Связанные требования:** FR-EQ-003

## Thought Process

NFC scanning automates equipment identification during return, reducing manual entry errors.

**Technical Approach:**

- Frontend uses Web NFC API or QR code scanning
- Backend receives UID and looks up equipment
- Validates equipment is currently rented
- Returns rental information for completion

**Error Scenarios:**

- UID not found in database
- Equipment status is not RENTED
- Multiple active rentals (shouldn't happen)

## Implementation Plan

- [ ] Create equipment lookup by NFC UID endpoint
- [ ] Implement validation logic
- [ ] Add error handling
- [ ] Frontend: Web NFC API integration
- [ ] Frontend: QR code fallback
- [ ] Create component tests
- [ ] Write unit tests

## Progress Tracking

**Overall Status:** Not Started - 0%

### Subtasks

| ID  | Description              | Status      | Updated    | Notes |
|-----|--------------------------|-------------|------------|-------|
| 3.1 | Create lookup endpoint   | Not Started | 2026-01-26 |       |
| 3.2 | Implement validation     | Not Started | 2026-01-26 |       |
| 3.3 | Add error handling       | Not Started | 2026-01-26 |       |
| 3.4 | Frontend NFC integration | Not Started | 2026-01-26 |       |
| 3.5 | Create tests             | Not Started | 2026-01-26 |       |

## Technical Details

**API Endpoint:**

- `POST /api/equipment/scan-return` - Scan NFC/QR for return
- Request: `{ "nfcUid": "ABC123XYZ" }`
- Response: Equipment details + active rental info

**Use Case:**

```java

@Service
public class ScanEquipmentForReturnUseCase {

    public EquipmentReturnInfo execute(String nfcUid) {
        Equipment equipment = equipmentRepository.findByNfcUid(nfcUid)
                .orElseThrow(() -> new EquipmentNotFoundException(nfcUid));

        if (equipment.getStatus() != EquipmentStatus.RENTED) {
            throw new EquipmentNotRentedException(equipment.getId());
        }

        Rental activeRental = rentalRepository
                .findActiveByEquipmentId(equipment.getId())
                .orElseThrow(() -> new NoActiveRentalException(equipment.getId()));

        return new EquipmentReturnInfo(equipment, activeRental);
    }
}
```

## References

- User Story File: [docs/tasks/us/US-EQ-003/us-eq-003.md](../../../docs/tasks/us/US-EQ-003/us-eq-003.md)
- Used by: US-RN-006
