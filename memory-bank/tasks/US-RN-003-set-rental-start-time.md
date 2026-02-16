# [US-RN-003] - Установка даты и времени начала проката (Set Rental Start Time)

**Status:** Deferred  
**Added:** 2026-02-16  
**Updated:** 2026-02-16  
**Priority:** Low (Deferred - Not Priority)  
**Module:** rental  
**Dependencies:** US-RN-001

## Original Request

**Как** Оператор проката  
**Я хочу** фиксировать точное время начала аренды  
**Чтобы** корректно рассчитывать стоимость аренды

## User Story Details

**Описание:**  
Система должна фиксировать точное время начала аренды.

**Критерии приемки:**

- Фиксация времени с точностью до секунды
- Автоматическое заполнение текущим временем
- Возможность редактирования (например, если клиент оформлял аренду заранее)

**Связанные требования:** FR-RN-003

## Decision: Deferred

**Дата решения:** 2026-02-16

**Причина отложения:**  
Функционал не является приоритетным. Время начала аренды (`startTime`) устанавливается автоматически при активации
аренды (US-RN-005) через метод `Rental.activate()`, который использует текущее время. Это покрывает основной use case.

**Текущая реализация:**

- При активации аренды (US-RN-005) время начала устанавливается автоматически через `LocalDateTime.now(clock)` в методе
  `UpdateRentalService.startRental()`
- Поле `startedAt` уже существует в domain model `Rental` и устанавливается при активации
- Это обеспечивает автоматическое заполнение текущим временем, что соответствует основному требованию

**Будущая реализация (если потребуется):**  
Если в будущем возникнет необходимость устанавливать время начала до активации аренды (например, для предварительного
бронирования), можно будет реализовать:

- Добавление `/startTime` в JSON Patch валидацию
- Метод `setStartTime()` в domain model `Rental`
- Обработку `startTime` в `UpdateRentalService`
- Использование предустановленного времени при активации, если оно было установлено

## Progress Tracking

**Overall Status:** Deferred - 0%

### Subtasks

| ID  | Description                                | Status   | Updated    | Notes        |
|-----|--------------------------------------------|----------|------------|--------------|
| 3.1 | Add startTime to JSON Patch validation     | Deferred | 2026-02-16 | Not priority |
| 3.2 | Add setStartTime() method to Rental domain | Deferred | 2026-02-16 | Not priority |
| 3.3 | Handle startTime in UpdateRentalService    | Deferred | 2026-02-16 | Not priority |
| 3.4 | Update activation logic to use preset time | Deferred | 2026-02-16 | Not priority |
| 3.5 | Create tests                               | Deferred | 2026-02-16 | Not priority |

## Progress Log

### 2026-02-16

- Task marked as Deferred
- Decision: функционал не приоритетен, так как время начала устанавливается автоматически при активации аренды
- Текущая реализация покрывает основной use case через US-RN-005

## References

- User Story File: [docs/tasks/us/US-RN-003/us-rn-003.md](../../../docs/tasks/us/US-RN-003/us-rn-003.md)
- Related: US-RN-005 (Start Rental) - устанавливает startTime автоматически при активации
- Dependencies: US-RN-001 (Create Rental Record)
