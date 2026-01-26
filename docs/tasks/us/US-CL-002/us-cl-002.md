# US-CL-002: Быстрое создание клиента

**Как** Оператор проката  
**Я хочу** быстро создать профиль клиента только с номером телефона  
**Чтобы** не задерживать процесс оформления аренды при отсутствии клиента в системе

**Описание:**  
Система должна позволять быстро создать профиль клиента с минимальными данными (только номер телефона).

**Критерии приемки:**

- Возможность создания клиента только с номером телефона
- Валидация формата номера телефона
- Автоматическое присвоение уникального ID клиента
- Время создания < 2 секунд

**Приоритет:** Высокий  
**Модуль:** client  
**Связанные требования:** FR-CL-002

---

## Subtasks

| Статус | Задача                    | Описание                                                 |
|--------|---------------------------|----------------------------------------------------------|
| ✅      | Create component test     | Create Cucumber feature test for quick customer creation |
| ✅      | Implement API endpoint    | POST /api/customers endpoint with phone validation       |
| ✅      | Implement validation      | Phone number format validation and normalization         |
| ✅      | Create database migration | Add customers table with Liquibase                       |
| 🆕     | Fix compilation errors    | Resolve Spring Security and repository access issues     |
| 🆕     | Run tests                 | Verify all tests pass                                    |

