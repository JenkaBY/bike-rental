# [US-EQ-002] - Добавление оборудования по порядковому номеру (Add Equipment by Serial Number)

**Status:** Completed  
**Added:** 2026-01-26  
**Updated:** 2026-02-05  
**Priority:** High  
**Module:** equipment  
**Dependencies:** US-EQ-001

## Original Request

**Как** Оператор проката  
**Я хочу** добавить оборудование в аренду по его порядковому номеру  
**Чтобы** быстро находить оборудование без сканирования QR-кода

## User Story Details

**Описание:**  
Система должна позволять добавить оборудование в аренду по его порядковому номеру.

**Критерии приемки:**

- Возможность ввода порядкового номера
- Проверка статуса "доступно"
- Отображение информации об оборудовании после выбора
- Время отклика < 1 секунды
- Поддержка автодополнения при вводе

**Связанные требования:** FR-EQ-002

## Thought Process

This user story enables the core rental workflow where operators select equipment for rental. Key considerations:

1. **User Experience**: Serial number (порядковый номер) is easier to remember and type than scanning QR codes
2. **Performance**: Sub-second response requires indexed search
3. **Autocomplete**: Enhances UX by suggesting as user types
4. **Status Check**: Must ensure equipment is AVAILABLE before adding to rental
5. **Integration**: Works with rental creation workflow (US-RN-001)

**Technical Approach:**

- Implement search endpoint that returns equipment by serial number
- Use database index on serial_number for fast lookup
- Return equipment details including current status
- Consider implementing prefix search for autocomplete (e.g., "12" matches "123", "124")
- Validate status is AVAILABLE before allowing selection

**Architecture Alignment:**

- Query operation in equipment module
- No domain events needed for lookup
- Response includes equipment details needed for rental

## Implementation Plan

- [x] Create search by serial number use case
- [x] Implement GET endpoint for equipment lookup by serial
- [x] Add status validation (AVAILABLE check)
- [x] Implement autocomplete/prefix search support
- [x] Create component tests for serial number lookup
- [x] Add unit tests for status validation
- [x] Write WebMvc tests for endpoint
- [x] Optimize query performance with EXPLAIN
- [x] Document API response format

## Progress Tracking

**Overall Status:** Completed - 100%

### Subtasks

| ID  | Description            | Status   | Updated    | Notes                                             |
|-----|------------------------|----------|------------|---------------------------------------------------|
| 2.1 | Create search use case | Complete | 2026-02-05 | Usecase: FindEquipmentBySerialUseCase implemented |
| 2.2 | Create tests           | Complete | 2026-02-05 | Unit, WebMvc and component tests added            |

## Progress Log

### 2026-02-05 (Task Completed)

- Implemented `FindEquipmentBySerialUseCase`.
- Added unit tests (status validation, use case), WebMvc tests (endpoint scenarios) and component tests (integration);
  all tests pass locally.
- Updated API documentation and added example responses.

## Technical Details

**Package Structure:**

```
com.github.jenkaby.bikerental.equipment
├── web.query
│   ├── EquipmentQueryController
│   └── dto.EquipmentLookupResponse
├── application
│   ├── usecase.FindEquipmentBySerialUseCase
└── domain
    └── repository.EquipmentRepository (add findBySerialNumber)
```

**API Endpoint:**

- `GET /api/equipment/search/serial/{serialNumber}`
- Path param: `serialNumber` (string)
- Query param (optional): `autocomplete=true` for prefix search
- Response: `200 OK` with equipment details
- Response format:

```json
{
  "id": "uuid",
  "serialNumber": "123",
  "uid": "uid",
  "equipmentType": "bicycle",
  "model": "Mountain Bike Pro",
  "status": "AVAILABLE",
  "condition": "Good"
}
```

- Error: `404 Not Found` if equipment not found
- Error: `400 Bad Request` if status is not AVAILABLE

**Database Query (example):**

```text
-- Exact match (example):
SELECT *
FROM equipment -- example table name
WHERE serial_number = :serialNumber
  AND status = 'AVAILABLE';

-- Autocomplete (prefix search, example):
SELECT *
FROM equipment -- example table name
WHERE serial_number LIKE :prefix || '%'
  AND status = 'AVAILABLE' LIMIT 10;
```

**Status Validation:**

- Only equipment with status AVAILABLE can be added to rental
- Return clear error message if equipment is in different status
- Include current status in error response for operator awareness

## Known Issues

None — task implemented and validated

## References

- User Story File: [docs/tasks/us/US-EQ-002/us-eq-002.md](../../../docs/tasks/us/US-EQ-002/us-eq-002.md)
- Architecture: [docs/backend-architecture.md](../../../docs/backend-architecture.md)
- Dependency: US-EQ-001 (Equipment catalog must be complete)
- Used by: US-RN-001 (Rental creation)
