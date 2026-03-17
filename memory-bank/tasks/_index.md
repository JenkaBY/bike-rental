# Tasks Index

<!--
Master list of all tasks with IDs, names, and current statuses.
Tasks are organized by status for easy tracking.
Task IDs correspond to User Story IDs from docs/tasks/user-stories.md
-->

[Task Priorities](../priorities.md) - URGENT / MEDIUM / LOW

## Completed

- [TECH-013] Unified Error Codes, CorrelationId Filter & i18n-Ready Problem Details - Completed on 2026-03-11
  (errorCode on all 14 domain exceptions, CorrelationIdFilter OncePerRequestFilter, correlationId replaces errorId in
  all 5 ControllerAdvice classes, structured errors array on 3 validation handlers, 11 new tests, i18n message keys)
- [TECH-012] Connect Spring Boot Actuator - Completed on 2026-03-05 (spring-boot-starter-actuator,
  application-management-config.yaml, actuator.feature component test)
- [TECH-011] Add Proprietary License (All Rights Reserved) - Completed on 2026-03-02 (LICENSE file in project root, License section in README.md, all dependencies verified as permissive-compatible)
- [TECH-010] CORS Filter with Configurable Allowed Origins - Completed on 2026-02-28 (CorsProperties
  @ConfigurationProperties, CorsConfig WebMvcConfigurer + CorsConfigurationSource bean, app.cors in application.yaml, 6
  tests: 4 unit + 2 WebMvc preflight)
- [TECH-009] Swagger / OpenAPI Annotations for All Controllers - Completed on 2026-02-28 (OpenApiConfig, @Tag per
  module, @Operation + @ApiResponses on all 28 endpoints, @Schema on 25 DTOs)
- [TECH-008] Continuous Deploy to Dev Environment - Completed on 2026-02-28 (Dockerfile, docker-compose app service,
  deploy.yml force-push CD to render-deploy branch, docs/deployment.md setup guide, no secrets required)
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
- [TECH-007] Добавление equipmentUid в таблицу rental - Completed on 2026-02-25 (rental module: добавлено поле
  equipment_uid в таблицу rentals, обновлены domain model и JPA entity, добавлена фильтрация по equipmentUid в GET
  /api/rentals, обновлены все сервисы для сохранения equipmentUid, полное покрытие тестами: unit, WebMvc, component)
- [US-RN-006] Возврат оборудования - Completed on 2026-02-26 (ReturnEquipmentService с 10-шаговым flow: поиск аренды
  по rentalId/equipmentUid/equipmentId, расчёт длительности и стоимости, запись доплаты, завершение аренды,
  публикация RentalCompleted; POST /api/rentals/return; RentalReturnResponse с CostBreakdown; WebMvc + component
  тесты rental-return.feature с 5 сценариями; единый метод TariffFacade.calculateRentalCost())
- [US-RN-010] Поддержка аренды нескольких единиц оборудования - Completed on 2026-03-17 (Marked complete by request;
  subtask 7.1 `UpdateRentalServiceTest` remains In Progress and must be recreated to restore unit test coverage; known
  issues recorded in task file)

## In Progress

<!-- no tasks in progress -->

## Pending - Technical Improvements

- [TECH-015] Fix Partial Equipment Return Calculation Logic - Pending - `ReturnEquipmentService.toPay` formula ignores
  remaining active equipment costs; `additionalPayment` shows wrong values in partial-return scenario; feature file has
  incorrect expected values (-200 / -100 instead of 0 / 100)
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
- [TECH-014] Improve negative scenario responses and tests - Medium priority, standardize error responses, add
  validation/missing-parameter details, update WebMvc and component negative tests (2026-03-12)

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
- [US-RN-008] Ранний возврат или замена оборудования - URGENT, Depends on US-RN-005

## Pending - Phase 4: Return & Calculations

- [US-TR-004] Эндпоинт расчёта стоимости аренды - High priority, tariff module, GET /api/tariffs/cost-estimate с
  разрешением по equipmentType / equipmentUid / tariffId, Depends on US-TR-002, US-RN-002
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
