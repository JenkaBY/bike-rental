# Tasks Index

<!--
Master list of all tasks with IDs, names, and current statuses.
Tasks are organized by status for easy tracking.
Task IDs correspond to User Story IDs from docs/tasks/user-stories.md
-->

[Task Priorities](../priorities.md) - URGENT / MEDIUM / LOW

## Completed

- [US-RN-001] Создание записи аренды - Completed on 2026-02-07 (rental module: domain model with RentalStatus enum,
  Fast Path and Draft Path creation, JSON Patch (RFC 6902) for updates, REST endpoints, domain exceptions, mappers,
  validators, unit/WebMvc/component tests)
- [US-TR-001] Справочник тарифов - Completed on 2026-02-03 (domain + application + web layers, tests, migrations)
- [US-EQ-001] Equipment Catalog - Completed on 2026-02-02 (139 subtasks: Database migrations,
  domain/infrastructure/application/web layers, 10 REST endpoints, WebMvc + Component tests, admin CRUD for
  types/statuses)
- [US-CL-003] Полное создание/редактирование профиля клиента - Completed on 2026-01-29 (PUT endpoint + mapper
  refactoring + 26 tests: 11 unit UpdateCustomerService, 15 WebMvc PUT, component tests)
- [US-CL-001] Поиск клиента по номеру телефона - Completed on 2026-01-28 (Implementation + unit, WebMvc, component
  tests)
- [US-CL-002] Быстрое создание клиента - Completed on 2026-01-27 (Implementation + 83+ tests: 68 unit, 15 WebMvc,
  component tests)
- [US-FN-001] Прием оплаты - Completed on 2026-02-04 (finance module: domain, application, infrastructure, web, tests)
- [US-EQ-004] Управление статусами оборудования - COMPLETED (2026-02-05) - URGENT, Depends on US-EQ-001
- [US-EQ-002] Добавление оборудования по порядковому номеру - Completed on 2026-02-05 (search by serial number,
  autocomplete, status validation, tests)
- [US-RN-002] Автоматический подбор тарифа - Completed on 2026-02-09 (improved selection algorithm with period-specific
  prices, RESTful endpoint GET /api/tariffs/selection, TariffPriceSelector utility, comprehensive tests)
- [US-RN-004] Внесение предоплаты - Completed on 2026-02-10 (prepayment recording with validation, integration with
  finance module, PaymentReceived event, comprehensive tests: unit, WebMvc, component)
- [US-RN-005] Запуск аренды - Completed on 2026-02-16 (rental activation via PATCH status=ACTIVE, RentalEventListener
  updates equipment status to RENTED, prepayment validation, component tests)
- [US-RN-007] Расчет времени аренды - Completed on 2026-02-18 (RentalDurationCalculator port in domain.service +
  RentalDurationCalculatorImpl in application.service, RentalDurationResult interface and BaseRentalDurationResult
  record in domain.service, application properties configuration app.rental.time-increment: 5m,
  calculateActualDuration() method in Rental entity using domain port, follows Dependency Inversion pattern like
  StatusTransitionPolicy)
- [US-RN-009] Просмотр активных аренд - Completed on 2026-02-18 (RESTful endpoint GET /api/rentals?status=ACTIVE,
  RentalSummaryResponse DTO, FindRentalsUseCase with filtering by status and customerId, RentalOverdueCalculator with
  Clock injection, RentalOverdueMapper and RentalQueryMapper, default sorting by expectedReturnAt, comprehensive tests:
  unit, WebMvc, component tests with RentalSummaryResponseTransformer, strict modular monolith architecture - no JOINs
  or enrichment)
- [US-TR-002] Расчет стоимости аренды - Completed on 2026-02-24 (Use Case pattern: CalculateRentalCostUseCase and
  CalculateRentalCostService, Strategy Pattern: ForgivenessStrategy and OvertimeCalculationStrategy for flexible
  business
  rules, RentalCost interface in module root, tariff selection based on actual duration, Money encapsulation with
  automatic
  rounding, configuration via RentalProperties, resolved cyclic dependencies, comprehensive parameterized unit tests,
  component test feature file prepared for future integration)
- [US-TR-003] Правило "прощения" просрочки - Completed on 2026-02-25 (MessageSource localization with properties files,
  Russian messages in messages_ru.properties, English fallback in messages.properties, integrated into
  ThresholdForgivenessStrategy via MessageSource.getMessage(), MessageSourceConfig with default locale ru, all unit
  tests updated with Russian messages)

## Pending - Technical Improvements

- [TECH-001] Custom UUID Generator for Hibernate Entity IDs - Medium priority, shared module, performance improvement
- [TECH-002] Integrate specification-arg-resolver for equipment filtering - Add advanced query param-based
  Specifications for equipment search (2026-02-02)
- [TECH-003] Add PATCH Endpoint for Equipment Status Change - Medium priority, equipment module, RESTful partial updates
  for status transitions (2026-02-02)
- [TECH-004] Rental Information Cache in Finance Module - Medium priority, finance module, event-driven rental
  validation (2026-02-04)
- [TECH-005] Tariff Selection Cache - Medium priority, tariff module, Spring Cache for tariff selection performance
  improvement (2026-02-09)
- [TECH-006] Integrate specification-arg-resolver for rental filtering - Medium priority, rental module, replace manual
  filtering logic with declarative Specifications (2026-02-18)

## Pending - Phase 1: Foundation (Core Stories)

- [US-AD-001] Управление пользователями - MEDIUM, admin module
- [US-AD-006] Резервное копирование и восстановление - LOW, admin module
- [US-TR-006] Tariff Versioning - LOW, tariff module (new task: implement immutable versioning)

## Pending - Phase 2: Basic Module Functions

- [US-CL-006] Customer Profile Change Audit Trail - LOW, customer module, depends on US-CL-003
- [US-FN-002] Возврат средств - Depends on US-FN-001
- [US-AD-002] Управление ролями и правами доступа - LOW, Depends on US-AD-001
- [US-AD-003] Настройка тарифов - LOW, Depends on US-TR-001
- [US-AD-004] Настройка бизнес-правил - URGENT, Depends on US-AD-001
- [US-MT-002] Учет ремонтов и обслуживания - LOW, Depends on US-EQ-001
- [US-MT-003] Вывод оборудования из эксплуатации - LOW, Depends on US-EQ-004

## Pending - Phase 3: Main Rental Process

- [US-RN-003] Установка даты и времени начала проката - DEFERRED (not priority, startTime устанавливается автоматически
  при старте аренды), Depends on US-RN-001
- [US-RN-006] Возврат оборудования - URGENT, Depends on US-RN-005, US-EQ-003, US-RN-007, US-TR-002
- [US-RN-008] Ранний возврат или замена оборудования - URGENT, Depends on US-RN-005

## Pending - Phase 4: Return & Calculations

- [US-TR-004] Расчет доплаты за просрочку - URGENT, Depends on US-TR-002, US-TR-003
- [US-EQ-003] Сканирование метки при возврате - URGENT, Depends on US-EQ-001, US-RN-005
- [US-EQ-005] Учет износа и пробега - LOW, Depends on US-RN-006
- [US-TR-005] Возврат средств при отмене - URGENT, Depends on US-RN-008, US-FN-002

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
