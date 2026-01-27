# Tasks Index

<!--
Master list of all tasks with IDs, names, and current statuses.
Tasks are organized by status for easy tracking.
Task IDs correspond to User Story IDs from docs/tasks/user-stories.md
-->

## In Progress

<!-- No tasks in progress currently -->

## Pending - Phase 1: Foundation (Core Stories)

- [US-CL-001] Поиск клиента по номеру телефона - High priority, customer module
- [US-CL-003] Полное создание/редактирование профиля клиента - High priority, customer module
- [US-EQ-001] Справочник оборудования - High priority, equipment module
- [US-TR-001] Справочник тарифов - High priority, tariff module
- [US-FN-001] Прием оплаты - High priority, finance module
- [US-AD-001] Управление пользователями - High priority, admin module
- [US-AD-006] Резервное копирование и восстановление - Low priority, admin module

## Pending - Phase 2: Basic Module Functions

- [US-EQ-002] Добавление оборудования по порядковому номеру - Depends on US-EQ-001
- [US-EQ-004] Управление статусами оборудования - Depends on US-EQ-001
- [US-FN-002] Возврат средств - Depends on US-FN-001
- [US-AD-002] Управление ролями и правами доступа - Depends on US-AD-001
- [US-AD-003] Настройка тарифов - Depends on US-TR-001
- [US-AD-004] Настройка бизнес-правил - Depends on US-AD-001
- [US-MT-002] Учет ремонтов и обслуживания - Depends on US-EQ-001
- [US-MT-003] Вывод оборудования из эксплуатации - Depends on US-EQ-004

## Pending - Phase 3: Main Rental Process

- [US-RN-001] Создание записи аренды - Depends on US-CL-001, US-CL-002, US-EQ-002, US-TR-001
- [US-RN-002] Автоматический подбор тарифа - Depends on US-TR-001
- [US-RN-003] Установка даты и времени начала проката - Depends on US-RN-001
- [US-RN-004] Внесение предоплаты - Depends on US-RN-001, US-FN-001
- [US-RN-005] Запуск аренды - Depends on US-RN-001, US-RN-004, US-EQ-004
- [US-RN-007] Расчет времени аренды - Depends on US-RN-003
- [US-RN-009] Просмотр активных аренд - Depends on US-RN-005

## Pending - Phase 4: Return & Calculations

- [US-TR-002] Расчет стоимости аренды - Depends on US-TR-001, US-RN-007
- [US-TR-003] Правило "прощения" просрочки - Depends on US-TR-002
- [US-TR-004] Расчет доплаты за просрочку - Depends on US-TR-002, US-TR-003
- [US-EQ-003] Сканирование NFC-метки при возврате - Depends on US-EQ-001, US-RN-005
- [US-RN-006] Возврат оборудования - Depends on US-RN-005, US-EQ-003, US-RN-007, US-TR-002
- [US-EQ-005] Учет износа и пробега - Depends on US-RN-006
- [US-RN-008] Ранний возврат или замена оборудования - Depends on US-RN-005
- [US-TR-005] Возврат средств при отмене - Depends on US-RN-008, US-FN-002

## Pending - Phase 5: Finance & History

- [US-FN-003] Финансовая история по аренде - Medium priority, finance module, depends on US-FN-001, US-FN-002,
  US-RN-004, US-RN-006
- [US-FN-004] Касса оператора - Medium priority, finance module, depends on US-FN-001, US-FN-002
- [US-CL-004] История аренд клиента - Low priority, customer module, depends on US-RN-001, US-RN-006
- [US-CL-005] Статистика по клиенту - Low priority, customer module, depends on US-CL-004

## Pending - Phase 6: Reporting & Analytics

- [US-RP-001] Отчет по доходам за период - Low priority, reporting module, depends on US-FN-001, US-FN-002
- [US-RP-002] Отчет по загрузке оборудования - Low priority, reporting module, depends on US-EQ-005, US-RN-006
- [US-RP-003] Финансовая сверка для бухгалтерии - Medium priority, reporting module, depends on US-FN-003, US-FN-004
- [US-RP-004] Аналитика по клиентам - Low priority, reporting module, depends on US-CL-005
- [US-RP-005] Дашборд оператора - Medium priority, reporting module, depends on US-RN-009, US-FN-001, US-EQ-001

## Pending - Phase 7: Technical Maintenance

- [US-MT-001] Планирование технического обслуживания - Low priority, maintenance module, depends on US-EQ-005
- [US-MT-004] Уведомления о технических проблемах - Low priority, maintenance module, depends on US-MT-001, US-RN-006

## Pending - Phase 8: Administration

- [US-AD-005] Журнал аудита - Low priority, admin module, depends on US-AD-001

## Blocked

<!-- No blocked tasks currently -->

## Completed

- [US-CL-002] Быстрое создание клиента - Completed on 2026-01-27 (Implementation + 83+ tests: 68 unit, 15 WebMvc,
  component tests)

## Abandoned

<!-- No abandoned tasks -->
