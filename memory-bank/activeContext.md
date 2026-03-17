# Active Context

<!--
Current work focus.
Recent changes.
Next steps.
Active decisions and considerations.

This file should contain:
- Current sprint/iteration focus
- Recently completed work
- Work in progress
- Immediate next steps
- Active technical decisions
- Blockers and dependencies
- Recent learnings
-->

## Current Focus

**Status:** 🚀 Active Implementation - US-RN-010 Почти завершён (~85%)  
**Date:** March 17, 2026  
**Phase:** Phase 4 - Return & Calculations + Technical Improvements  
**Branch:** `feature/support-rental-of-equipment-group`

### Primary Objective

US-RN-010 (Поддержка аренды нескольких единиц оборудования) — основная реализация **завершена** на ветке
`feature/support-rental-of-equipment-group`. Все 7 ключевых subtask'ов выполнены кроме пересоздания
`UpdateRentalServiceTest`. После закрытия тестового долга — следующий приоритет: TECH-015 (исправление
формулы частичного возврата), затем US-TR-004.

### Current Activities

1. **US-RN-010: Поддержка аренды нескольких единиц оборудования** 🔄 IN PROGRESS ~85% (March 12–17, 2026)

   **Реализовано:**
    - DB: таблица `rental_equipments` создана; `equipment_id`/`equipment_uid` удалены из `rentals`; FK + индексы
    - Domain: `RentalEquipment` child entity + `RentalEquipmentStatus` (ASSIGNED/ACTIVE/RETURNED); `Rental` переработан
    - JPA: `RentalEquipmentJpaEntity` + `@OneToMany(cascade=ALL)` в `RentalJpaEntity`; computed cost methods
    - Services: `CreateRentalService` — per-equipment tariff+cost; `UpdateRentalService` — валидация новых equipment;
      `ReturnEquipmentService` — частичный возврат, per-equipment cost, завершение при `allEquipmentReturned()`
    - Events: `RentalCreated`/`RentalStarted`/`RentalCompleted` расширены списками IDs; новый `RentalUpdated` для draft
    - Equipment module: `RentalEventListener` поддерживает списки; `EquipmentFacade.findByIds()`
    - Finance module: `FinanceFacade.getPayments()`; `PaymentType.ADDITIONAL_PAYMENT`
    - Web DTOs: `CreateRentalRequest.equipmentIds: List<Long>`, `ReturnEquipmentRequest` списки, `RentalResponse` с
      `List<EquipmentItemResponse>`; новые `PaymentInfoResponse`, `CostBreakdown` list в `RentalReturnResponse`
    - WebMvc tests: `RentalCommandControllerTest` обновлён (219 изменений)
    - Component tests: `rental.feature`, `rental-return.feature`, `rental-query.feature`, `rental-validation.feature`

   **Открытые проблемы:**
    - ⚠️ TECH-015: формула `toPay` некорректна при частичном возврате

2. **TECH-013: Unified Error Codes, CorrelationId Filter & i18n-Ready Problem Details** ✅ COMPLETED (March 11, 2026)
    - `BikeRentalException` — поле `errorCode` + `getErrorCode()`, новый protected конструктор
   - `CorrelationIdFilter` — `@Component OncePerRequestFilter`, X-Correlation-ID в MDC
   - Все 5 ControllerAdvice классов унифицированы
    - 11 новых тестов: `CorrelationIdFilterTest` (4) + `CoreExceptionHandlerAdviceTest` (7)

3. **TECH-010: CORS Filter** ✅ COMPLETED (February 28, 2026)
4. **TECH-009: Swagger / OpenAPI Annotations** ✅ COMPLETED (February 28, 2026)
5. **TECH-008: Continuous Deploy** ✅ COMPLETED (February 28, 2026)

### Next Priority Tasks

1. **TECH-015: Fix Partial Equipment Return Calculation** — URGENT
    - Исправить формулу `toPay` в `ReturnEquipmentService`
    - Исправить ожидаемые значения в feature file

2. **US-TR-004: Cost Estimate Endpoint** — HIGH PRIORITY
    - GET /api/tariffs/cost-estimate
    - Разрешение по `equipmentType` / `equipmentUid` / `tariffId`
    - Depends on US-TR-002 ✅, US-RN-002 ✅

3. **US-RN-008: Early Return / Equipment Swap** — URGENT
    - Depends on US-RN-005 ✅


## Recent Changes

### In Progress (March 12–17, 2026)

**US-RN-010: Поддержка аренды нескольких единиц оборудования** 🔄 ~85%

101 файл изменён, 2811 добавлено / 1397 удалено в 20 коммитах на ветке
`feature/support-rental-of-equipment-group`.

**DB & Infrastructure:**

- `rental-equipments` таблица создана (`rental-equipments.create-table.xml`)
- `equipment_id` и `equipment_uid` удалены из таблицы `rentals` (были в `TECH-007`, теперь перенесены)
- `RentalEquipmentJpaEntity` — новый JPA entity, `@ManyToOne(fetch=LAZY)` к `RentalJpaEntity`
- `RentalJpaEntity` — `@OneToMany(cascade=ALL, orphanRemoval=true)`, `@Fetch(SUBSELECT)`; computed
  `getEstimatedCost()` / `getFinalCost()`
- `RentalEquipmentJpaMapper`, `RentalEquipmentMapper`, `RentalEquipmentStatusMapper`, `RentalEquipmentWebMapper` — новые
  MapStruct маперы
- `PatchValueParser` — утилита парсинга JSON Patch значений

**Domain:**

- `RentalEquipment` — child entity: equipmentId, equipmentUid, tariffId, status, startedAt, expectedReturnAt,
  actualReturnAt, estimatedCost, finalCost; методы `activateForRental()`, `markReturned()`, `assigned()`
- `RentalEquipmentStatus` — enum: ASSIGNED / ACTIVE / RETURNED
- `Rental` — заменён единый equipmentId на `List<RentalEquipment> equipments`; новые методы: `addEquipment()`,
  `clearEquipmentRentals()`, `allEquipmentReturned()`, `equipmentsToReturn()`; `getEstimatedCost()` суммирует по
  оборудованию

**Application Services:**

- `CreateRentalService` — per-equipment tariff selection + cost;
  `RequestedEquipmentValidator.validateSize() / validateAvailability()`
- `UpdateRentalService` — `equipmentIds: List<Long>`; валидация только НОВЫХ (не уже зарезервированных) equipment; новая
  зависимость `PatchValueParser`
- `ReturnEquipmentService` — частичный возврат по спискам equipmentIds/equipmentUids; per-equipment стоимость; доплата =
  previouslyReturnedCost + currentCost + remainingEstimated - allPayments; завершение аренды только при
  `allEquipmentReturned()`

**Events:**

- `RentalCreated` — теперь `List<Long> equipmentIds`
- `RentalStarted` — теперь `List<Long> equipmentIds`
- `RentalCompleted` — теперь `List<Long> equipmentIds` + `List<Long> returnedEquipmentIds`
- `RentalUpdated` (новый) — `RentalState(rentalStatus, equipmentIds)` для `previousState` и `currentState`;
  синхронизация статусов при изменении draft-аренды

**Equipment Module:**

- `RentalEventListener` — обработчик для всех событий переведён на списки IDs; новый `onRentalUpdated()` для управления
  статусами при черновике
- `GetEquipmentByIdsUseCase` + `GetEquipmentByIdsService` — новый use case
- `EquipmentFacade.findByIds()`, `EquipmentRepository.findByIds()`, `EquipmentRepositoryAdapter`,
  `EquipmentJpaRepository` — обновлены

**Finance Module:**

- `FinanceFacade.getPayments(Long rentalId)` + `FinanceFacadeImpl` — метод для получения всех платежей по аренде
- `PaymentType.ADDITIONAL_PAYMENT` — новый тип платежа

**Web Layer:**

- `CreateRentalRequest` — `List<Long> equipmentIds` вместо единственного `equipmentId`
- `ReturnEquipmentRequest` — `List<Long> equipmentIds`, `List<String> equipmentUids`; валидация через `@AssertTrue`
- `RentalResponse` — `List<EquipmentItemResponse> equipmentItems` вместо единственных equipment полей
- `EquipmentItemResponse` (новый) — equipmentId, equipmentUid, estimatedCost, finalCost, tariffId, status
- `PaymentInfoResponse` (новый) — DTO для платёжной информации
- `RentalReturnResponse` — `List<CostBreakdown> costs`; `PaymentInfoResponse paymentInfo`
- `RentalRestControllerAdvice` — обработчик `InvalidRentalPlannedDurationException`

**Tests:**

- `RentalCommandControllerTest` — переработан (219 изменений): новые сценарии POST/PATCH с массивами
- `rental.feature`, `rental-return.feature`, `rental-query.feature`, `rental-validation.feature` — полностью обновлены
- Новая инфраструктура component tests: `RentalDbSteps`, `RentalEquipmentJpaEntityTransformer`,
  `EquipmentItemResponseTransformer`, `RentalReturnCostBreakdownTransformer`, `RentalReturnExpectationTransformer`
- `RecordPrepaymentServiceTest`, `FindRentalsServiceTest`, `RentalTest` — обновлены
- ⚠️ `UpdateRentalServiceTest` **удалён** (493 строки) — нужно пересоздать

---

### Completed (March 11, 2026)

**TECH-013: Unified Error Codes, CorrelationId Filter & i18n-Ready Problem Details** ✅ COMPLETED

**Implementation:**

- `CorsProperties` record — `@ConfigurationProperties(prefix = "app.cors")`, поля: `allowedOrigins` (`@NotEmpty`,
  обязательный), `allowedMethods` (`@DefaultValue` GET/POST/PUT/PATCH/DELETE/OPTIONS), `allowedHeaders` (
  `@DefaultValue` *), `allowCredentials` (`@DefaultValue` true), `maxAge` (`@DefaultValue` 3600)
- `CorsConfig` — `@Configuration` + `WebMvcConfigurer.addCorsMappings("/**")` + `CorsConfigurationSource` бин
- `application.yaml` дополнен секцией `app.cors` (dev: localhost:3000, localhost:5173)
- `application-test.yaml` дополнен секцией `app.cors`
- Авто-регистрация через `@ConfigurationPropertiesScan` в `BikeRentalApplication`

**Testing:**

- `CorsConfigTest` — 4 unit-теста: origins, methods, credentials/maxAge, wildcard paths
- `CorsPreflightTest` — 2 WebMvc preflight-теста: разрешённый origin → 200 + заголовки; запрещённый → нет
  `Access-Control-Allow-Origin`
- Все 6 тестов прошли `BUILD SUCCESSFUL`

---

**2. TECH-009: Swagger / OpenAPI Annotations** ✅ COMPLETED (February 28, 2026)

**Implementation:**

- `OpenApiConfig` с глобальными настройками API
- `@Tag` на всех 14 контроллерах (группировка по модулям)
- `@Operation` + `@ApiResponses` на всех 28 эндпоинтах (включая 400, 404, 409, 422, 500)
- `@Schema` на 25 DTO

---

**3. TECH-008: Continuous Deploy to Dev Environment** ✅ COMPLETED (February 28, 2026)

**Implementation:**

- `Dockerfile` для fat JAR
- docker-compose app service
- `deploy.yml` — GitHub Actions CD, force-push на render-deploy branch
- `docs/deployment.md` — setup guide

---

### Completed (February 26, 2026)

**1. US-RN-006: Equipment Return** ✅ COMPLETED

**Implementation:**

- `ReturnEquipmentService` с 10-шаговым flow: поиск аренды по rentalId/equipmentUid/equipmentId, расчёт длительности и
  стоимости, запись доплаты, завершение аренды, публикация `RentalCompleted`
- POST /api/rentals/return эндпоинт
- `RentalReturnResponse` с `CostBreakdown`
- `TariffFacade.calculateRentalCost()` — единый метод для расчёта

**Testing:**

- WebMvc тесты
- Component тесты `rental-return.feature` с 5 сценариями

---

### Completed (February 25, 2026)

**1. TECH-007: Equipment UID in Rental Table** ✅ COMPLETED

- Поле `equipment_uid` в таблице `rentals`
- Обновлены domain model и JPA entity
- Фильтрация по `equipmentUid` в GET /api/rentals
- Полное покрытие тестами: unit, WebMvc, component

**2. US-TR-003: Forgiveness Rule Localization** ✅ COMPLETED

- `MessageSource` с `AcceptHeaderLocaleResolver`
- `messages.properties` (EN) и `messages_ru.properties` (RU) с UTF-8
- `MessageService` интерфейс и реализация
- Все 17 unit-тестов прошли

---

### Completed (February 18, 2026)

**1. US-RN-007: Rental Duration Calculation** ✅ COMPLETED

**Implementation:**

- RentalDurationCalculator port interface in domain.service (follows Dependency Inversion pattern)
- RentalDurationCalculatorImpl implementation in application.service
- RentalDurationResult interface and BaseRentalDurationResult record in domain.service
- RentalProperties with @ConfigurationProperties(prefix = "app.rental")
- Application property: app.rental.time-increment: 5m
- calculateActualDuration() method added to Rental entity (uses domain port)
- Comprehensive parameterized unit tests using @ParameterizedTest with @ValueSource, @CsvSource, @MethodSource

**Architecture:**

- Domain layer defines RentalDurationCalculator port (interface)
- Application layer provides RentalDurationCalculatorImpl implementation
- Domain model (Rental) depends only on domain interfaces, not application layer
- Follows same pattern as StatusTransitionPolicy in equipment module

**Key Features:**

- Formula: (actualMinutes + increment - 1) / increment * increment for rounding up
- Supports durations from 0 minutes to multiple days
- actualMinutes computed from actualDuration (no redundant storage)
- Configuration-ready for future app.rental.forgiveness.overtime-duration property

**Testing:**

- Parameterized tests covering all edge cases (small, medium, large values)
- Tests for cancellation window (US-RN-008 integration)
- Tests with different time increments (5m, 10m, 15m)
- Tests for very long durations (multiple days)
- All tests passing

**Quality Metrics:**

- Implementation: ~150 lines
- Tests: ~200 lines (parameterized)
- Test-to-code ratio: 1.3:1
- Zero compilation errors

**Timeline:** 1 day (Feb 18, 2026), 9/9 subtasks completed

---

### Completed (February 6, 2026)

**1. Equipment Module DDD Refactoring** ✅ COMPLETED

**Architecture Improvement:**

- Refactored Equipment domain model to follow DDD principles correctly
- Changed Equipment from storing `EquipmentStatus` Entity to `statusSlug: String` (Value Object)
- Created `StatusTransitionPolicy` port interface in domain layer
- Implemented `EquipmentStatusTransitionPolicy` in application layer
- Removed `EquipmentJpaMapperDecorator` - direct mapping eliminates N+1 queries
- Updated all mappers and services to work with `statusSlug`

**Key Benefits:**

- ✅ Performance: Eliminated N+1 queries when loading Equipment entities
- ✅ DDD Compliance: EquipmentStatus is now correctly modeled as separate Reference Data Aggregate
- ✅ Domain Purity: Equipment uses port for validation, no infrastructure dependencies
- ✅ Maintainability: Clear separation of concerns between aggregates

**Documentation Updated:**

- Updated `US-EQ-004-equipment-status-management.md` with new architecture
- Updated `docs/backend-architecture.md` to reflect correct domain model
- Updated `progress.md` with refactoring details

---

### Completed (February 4, 2026)

**1. US-FN-001: Payment Acceptance** ✅ COMPLETED

**Implementation:**

- Domain, infrastructure, application, web layers implemented
- Receipt generation, UUID generator port, event publishing
- WebMvc and component tests added

**Quality Metrics:**

- Implementation: ~400 lines
- Tests: ~900 lines
- Test-to-code ratio: 2.25:1
- Zero compilation errors
- All tests passing

**Timeline:** 1 day (Feb 4, 2026), 11/11 subtasks completed

---

### Completed (January 29, 2026)

**1. US-CL-003: Full Customer Profile Management** ✅ COMPLETED

**Implementation:**

- PUT /api/customers/{id} endpoint for full profile updates
- Extended Customer domain model with comments field
- UpdateCustomerService with validation (existence, phone uniqueness)
- Unified CustomerRequest DTO for both POST and PUT
- Database migration for comments column

**Mapper Refactoring (Major Improvement):**

- Created shared VO mappers in customer.shared.mapper:
    - PhoneNumberMapper (String ↔ PhoneNumber)
    - EmailAddressMapper (String ↔ EmailAddress)
- Refactored 5 mappers to use shared VO mappers:
    - CustomerCommandToDomainMapper (auto-generated mappings)
    - CustomerCommandMapper (MapStruct-generated toUpdateCommand)
    - CustomerJpaMapper (simplified, removed manual methods)
    - CustomerMapper (uses VO mappers)
    - CustomerQueryMapper (no changes needed)
- Benefits: DRY principle, single source of truth, reduced code duplication

**Testing:**

- 11 Unit tests for UpdateCustomerService (all scenarios covered)
- 15 WebMvc tests for PUT endpoint (validation and errors)
- Component tests with consolidated BDD feature
- CustomerResponseTransformer for cleaner assertions
- Email validation tests (6 invalid formats)

**Additional Improvements:**

- Added @Email validation annotation to CustomerRequest
- Added controller logging for POST, PUT, GET endpoints
- Format: [HTTP_METHOD] Action identifier
- Updated CreateCustomerServiceTest with new dependencies
- Fixed all compilation errors

**Quality Metrics:**

- Implementation: ~400 lines
- Tests: ~900 lines
- Test-to-code ratio: 2.25:1
- Zero compilation errors
- All tests passing

**Timeline:** 1 day (Jan 29, 2026), 11/11 subtasks completed

---

### Completed (January 28, 2026)

**1. US-CL-001: Customer Search by Phone** ✅ COMPLETED

**Implementation:**

- GET /api/customers/search endpoint with validation
- Application query use case with configurable result limit
- Repository search by partial phone with limit

**Testing:**

- Unit tests for normalization and mapping
- WebMvc tests for valid and invalid inputs
- Component test for search scenarios and limit

---

**2. Modulith Architecture Tests Stabilized**

- Updated `ModulithBoundariesTest` layered architecture rules to reflect module root API layer usage
- Ignored external library dependencies (Spring, MapStruct, Lombok, JDK) for infrastructure and module API layers
- Reduced false positives in hexagonal architecture checks while preserving domain and web boundaries

---

### Completed (January 27, 2026)

**1. US-CL-002: Quick Customer Creation** ✅ COMPLETED

**Implementation:**

- POST /api/customers endpoint with full validation and error handling
- Domain: Customer aggregate with PhoneNumber and EmailAddress value objects
- Application: CreateCustomerUseCase with duplicate phone detection
- Infrastructure: JPA repository with Liquibase migration
- Web: CustomerCommandController with comprehensive validation

**Testing:**

- 68 Unit tests (service, domain, utilities) - All passing
- 15 WebMvc tests (controller validation with @ApiTest) - All passing
- Component tests (Cucumber BDD) - All passing
- Total: 83+ automated tests with TDD approach

**Quality:**

- Zero compilation errors
- Follows hexagonal architecture
- Complete JavaDoc documentation
- Clean code with 2.4:1 test-to-code ratio

**Timeline:** 6 days (Jan 21-27, 2026), 12/12 subtasks completed

---

### Completed (January 26, 2026)

**1. Memory Bank Infrastructure** ✅

- Created memory-bank/ folder structure
- Established tasks/ subfolder for user story tracking
- Created _index.md for task organization by phase

**2. Complete User Story Migration** ✅

- **Phase 1 (Foundation)**: 8 tasks migrated
    - Customer management (search, creation, profile)
    - Equipment catalog and status
    - Tariff management
    - Admin functionality (users, backup)
- **Phase 2 (Basic Functions)**: 8 tasks migrated
    - Role and permission management
    - Tariff configuration
    - Business rules configuration
    - Equipment addition by serial number
    - Equipment status management
    - Payment acceptance
    - Refund processing
    - Maintenance records and decommissioning
- **Phase 3 (Rental Process)**: 7 tasks migrated
    - Rental creation workflow
    - Automatic tariff selection
    - Start time setting
    - Prepayment recording
    - Rental activation
    - Duration calculation
    - Active rentals dashboard
- **Phase 4 (Return & Calculations)**: 8 tasks migrated
    - Cost calculation with business rules
    - Forgiveness rule implementation
    - Overtime charge calculation
  - Tag scanning on return
    - Equipment return workflow
    - Equipment usage tracking
    - Early return/replacement
    - Refund on cancellation
- **Phase 5 (Finance & History)**: 4 tasks migrated
    - Rental financial history
    - Operator cash register management
    - Customer rental history
    - Customer statistics and loyalty
- **Phase 6 (Reporting & Analytics)**: 5 tasks migrated
    - Revenue reports
    - Equipment utilization reports
    - Financial reconciliation
    - Customer analytics
    - Operator dashboard
- **Phase 7 (Technical Maintenance)**: 2 tasks migrated
    - Maintenance scheduling (automatic)
    - Technical issue notifications
- **Phase 8 (Administration)**: 1 task migrated
    - Audit logging system

**3. Foundation Files Completed** ✅

- projectbrief.md (298 lines): Project scope, objectives, stakeholders, success criteria, constraints
- systemPatterns.md (568 lines): Architecture overview, 17 design patterns, communication models, API design, security
- productContext.md (579 lines): Problem statement, 5 user types, 5 workflows, feature requirements, UX goals, 20
  business rules
- techContext.md (935 lines): Complete tech stack, development setup, build process, testing strategy

**4. Task Index Organization** ✅

- All 43 tasks organized in _index.md
- Grouped by phase (Pending status for all)
- Dependencies clearly documented

### Documentation Statistics

**Total Documentation Created:**

- 43 task files (~200 KB)
- 4 core foundation files (~72 KB)
- 1 task index file
- **Total:** ~272 KB of comprehensive documentation
- **Total Lines:** 2,380 lines (foundation) + ~5,000 lines (tasks) = ~7,380 lines

## Work in Progress

### Current Status: DOCUMENTATION COMPLETE ✅

**Nothing in active development** - All documentation work is complete and the project is ready for implementation
phase.

### Documentation Validation

All files have been created and populated:

- ✅ Memory Bank folder structure
- ✅ All 43 user story task files
- ✅ Task index (_index.md)
- ✅ Project brief (projectbrief.md)
- ✅ System patterns (systemPatterns.md)
- ✅ Product context (productContext.md)
- ✅ Technical context (techContext.md)
- ✅ Active context (activeContext.md) - This file

### Quality Assurance

**Task Files Quality:**

- All files follow Memory Bank structure
- Implementation plans with subtasks
- Progress tracking tables
- Technical details with code examples
- Dependency mapping
- References to source documentation

**Foundation Files Quality:**

- Comprehensive coverage of all aspects
- Clear, actionable information
- Cross-referenced with user stories
- Production-ready documentation

## Next Steps

### Immediate (Next Session)

**1. Continue Phase 1 Foundation Implementation**

**Next Priority Tasks:**

- **US-CL-003: Full Customer Profile** (HIGH PRIORITY)
  - Complete customer management capabilities
  - PUT /api/customers/{id} endpoint
  - GET /api/customers/{id} endpoint

- **US-EQ-001: Equipment Catalog** (HIGH PRIORITY)
  - Critical for rental workflow
  - New equipment module
  - Equipment domain model with status management

- **US-TR-001: Tariff Catalog** (HIGH PRIORITY)
  - Essential for pricing configuration
  - New tariff module
  - Tariff domain model with rules and rates

**2. Development Best Practices (Continue)**

Apply same TDD approach as US-CL-002:

- Write component tests first (Cucumber)
- Implement with unit tests
- Add WebMvc tests for validation
- Maintain test-to-code ratio > 2:1
- Complete JavaDoc documentation

### Short Term (Next 2-4 Weeks)

**Complete Phase 1: Foundation** (3 remaining tasks of 7)

- [x] US-CL-002: Quick Customer Creation ✅ DONE
- [x] US-CL-001: Customer Search by Phone ✅ DONE
- [x] US-CL-003: Full Customer Profile ✅ DONE
- [ ] US-EQ-001: Equipment Catalog
- [ ] US-TR-001: Tariff Catalog
- [ ] US-AD-001: User Management

**Success Criteria:**

- Operator can search/create customers
- Operator can view equipment catalog
- Basic user authentication works
- Payment recording functional

### Medium Term (1-3 Months)

**Complete Phases 2-3: Core Rental Process**

- Phase 2: Enhanced configuration and status management (8 tasks)
- Phase 3: Complete rental workflow from creation to return (7 tasks)

**Success Criteria:**

- End-to-end rental workflow operational
- Cost calculations working correctly
- Business rules (forgiveness, overtime) implemented
- tag scanning functional

### Long Term (3-6 Months)

**Complete Phases 4-8: Advanced Features**

- Phase 4: Return processing with complex calculations (8 tasks)
- Phase 5: Financial history and customer insights (4 tasks)
- Phase 6: Reporting and analytics (5 tasks)
- Phase 7: Maintenance automation (2 tasks)
- Phase 8: Audit and compliance (1 task)

**Success Criteria:**

- All 43 user stories implemented
- Full test coverage
- Production-ready system
- Complete reporting suite

## Active Decisions

### Recently Decided ✅

**1. Architecture Approach**

- **Decision:** Spring Modulith for modular monolith
- **Rationale:** Simplified deployment, minimal infrastructure, easy migration to microservices later
- **Impact:** Single JAR deployment, strict module boundaries, event-driven communication
- **Status:** Documented in systemPatterns.md

**2. Testing Strategy**

- **Decision:** Test-Driven Development (TDD) with 3-tier testing
- **Rationale:** Ensure quality, prevent regression, living documentation
- **Approach:**
    - Component tests: Positive scenarios, full stack with Testcontainers
    - Unit tests: Business logic, mocked dependencies
    - WebMvc tests: Request validation
- **Status:** Documented in techContext.md

**3. Domain-Driven Design**

- **Decision:** DDD with clear bounded contexts per module
- **Rationale:** Align code with business domains, maintainable architecture
- **Implementation:** Each module = bounded context with aggregate roots
- **Status:** Documented in systemPatterns.md

**4. Event-Driven Communication**

- **Decision:** Spring Application Events for inter-module communication
- **Rationale:** Loose coupling, async processing, eventual consistency
- **Trade-off:** Debugging complexity, but better scalability
- **Status:** Documented in systemPatterns.md

**5. Business Rules Configuration**

- **Decision:** Configurable rules via database (SystemSettings)
- **Key Rules:**
    - Time increment: 5 minutes
    - Forgiveness threshold: 7 minutes
    - Overtime initial rounding: 10 minutes
    - Cancellation window: 10 minutes
- **Rationale:** Allow business flexibility without code changes
- **Status:** Documented in productContext.md (20 business rules)

### Pending Decisions

**None** - All architectural and technical decisions have been made and documented.

### Future Decisions (Implementation Phase)

**1. Database Migration Strategy**

- Need to decide: Flyway vs Liquibase
- Current default: Flyway (already in tech stack)
- Decision point: First database schema creation

**2. Frontend Framework**

- Current: Backend-focused, minimal frontend
- Future: May need React/Vue for operator dashboard
- Decision point: When UI complexity increases

**3. Caching Strategy**

- Current: Spring Cache abstraction
- Future: May need Redis for distributed caching
- Decision point: When performance requires it

**4. Deployment Strategy**

- Current: Fat JAR on single server
- Future: Docker containers, Kubernetes if scaling needed
- Decision point: When load increases beyond single server

## Blockers

### Current Blockers

**None** - Documentation phase complete, ready for implementation.

### Resolved Blockers

**1. Incomplete Architecture Documentation** ✅ RESOLVED

- Was: Architecture details scattered across multiple files
- Resolution: Comprehensive systemPatterns.md with all architectural decisions
- Date Resolved: January 26, 2026

**2. Missing User Story Details** ✅ RESOLVED

- Was: User stories lacked implementation guidance
- Resolution: All 43 user stories migrated with thought process, technical details, implementation plans
- Date Resolved: January 26, 2026

**3. Unclear Business Rules** ✅ RESOLVED

- Was: Business rules not clearly documented
- Resolution: 20 comprehensive business rules documented in productContext.md
- Date Resolved: January 26, 2026

### Potential Future Blockers

**1. Testing Environment Setup**

- Risk: Testcontainers may require Docker configuration
- Mitigation: techContext.md has complete setup guide
- Severity: Low (well-documented)

**2. MapStruct Configuration**

- Risk: Annotation processing setup in IDE
- Mitigation: IDE configuration documented in techContext.md
- Severity: Low (standard setup)

**3. Spring Modulith Learning Curve**

- Risk: Team unfamiliar with Spring Modulith
- Mitigation: Patterns documented, examples in documentation
- Severity: Medium (new technology)

## Recent Learnings

### Documentation Phase Insights

**1. Memory Bank Structure is Powerful**

- Comprehensive documentation enables AI-assisted development
- Clear task structure with thought process aids implementation
- Progress tracking built-in from the start

**2. User Story Migration Benefits**

- Converting scattered docs to structured tasks reveals gaps
- Dependency mapping highlights critical path
- Implementation plans force architectural thinking

**3. Business Rules Need Explicit Documentation**

- 20 business rules extracted and formalized
- Examples with calculations (e.g., "8 minutes late → 10 minutes charge")
- Configuration vs hard-coded decisions clarified

**4. Module Boundaries Are Critical**

- 8 modules with clear responsibilities
- Event-driven communication prevents tight coupling
- Facade pattern for inter-module APIs

**5. Three-Layer Mapping Strategy**

- Web DTO ↔ Command/Query ↔ Domain ↔ JPA Entity
- Prevents domain pollution from infrastructure
- MapStruct handles type-safe conversion

### Technical Insights

**1. Spring Modulith Advantages**

- Enforces module boundaries in monolith
- Easy path to microservices if needed
- Test support for validating architecture

**2. TDD Approach Clarity**

- Three test types serve different purposes
- Component tests use `test` profile (important!)
- Testcontainers for real database testing

**3. Cost Calculation Complexity**

- Time rounding + forgiveness + overtime = complex logic
- Requires extensive unit test coverage
- Configuration makes it flexible for business changes

**4. Event-Driven Design Benefits**

- Clean separation of concerns
- Easy to add new listeners without modifying publishers
- Eventual consistency acceptable for most operations

**5. Domain Model Richness**

- Value Objects (Money, RentalDuration) enforce invariants
- Aggregate Roots control transactions
- Domain Events capture state changes

### Process Insights

**1. Documentation-First Approach**

- Complete docs before coding reduces rework
- AI can assist better with full context
- Team alignment easier with written plans

**2. Phase-Based Delivery**

- 8 phases provide clear milestones
- Dependencies between phases guide implementation order
- Early phases deliver core value

**3. Task Granularity**

- 43 tasks is manageable scope
- Each task has 5-8 subtasks for tracking
- Clear acceptance criteria per task

---

**Last Updated:** February 18, 2026  
**Status:** Documentation Complete ✅ | Active Implementation 🚀  
**Next Review:** At start of next implementation phase

## Recent Changes (2026-02-18 - Architecture Fix)

### US-RN-007: Architecture Compliance Fix

**What Changed:**

- Refactored RentalDurationCalculator to follow Dependency Inversion pattern
- Created port interface in domain.service (RentalDurationCalculator)
- Moved implementation to application.service (RentalDurationCalculatorImpl)
- Moved RentalDurationResult and BaseRentalDurationResult to domain.service
- Domain model (Rental) now depends only on domain interfaces

**Architecture Pattern:**

- Follows same pattern as StatusTransitionPolicy in equipment module
- Domain layer defines contracts (ports), application layer provides implementations
- Ensures domain layer never depends on application layer
- Fixes hexagonal architecture violations detected by ModulithBoundariesTest

**Impact:**

- ✅ Architecture tests now pass
- ✅ Better separation of concerns
- ✅ Domain layer remains pure (no application dependencies)
- ✅ Easier to test domain logic with mock implementations

## 2026-01-30

- Component tests for equipment status endpoints (list, get by ID, update status) implemented and ready for execution
- InsertableEquipmentRepository created for test DB setup in component tests
- All step definitions and feature file follow project and Cucumber BDD standards
- Next: Execute tests and review results, then proceed to further equipment module scenarios

## Recent Changes (2026-02-04)

### BikeRentalEvent Marker Interface - Architectural Standard Established

**What Changed:**

- Established architectural rule: ALL domain events MUST implement
  `com.github.jenkaby.bikerental.shared.domain.event.BikeRentalEvent`
- Updated architecture documentation across multiple files
- Fixed component test event handling infrastructure

**Documentation Updates:**

1. **backend-architecture.md** - Added section 5.2 with BikeRentalEvent requirement, code examples, benefits, and usage
   patterns
2. **systemPatterns.md** - Enhanced Domain Events pattern section with BikeRentalEvent contract, implementation
   examples, and benefits
3. **event-architecture.md** (NEW) - Comprehensive 300+ line guide covering:
    - Interface purpose and location
    - Usage examples (events, listeners, test capture)
    - Current domain events across modules
    - Benefits (type safety, filtering, generic processing, testing)
    - Implementation checklist
    - Anti-patterns to avoid
    - Enforcement strategy

**Code Changes:**

1. **MessageStore.java** - Added BikeRentalEvent filtering logic with debug logging for non-domain events
2. **TestMessageListener.java** - Fixed import to use @EventListener (Spring standard event listener)

**Current Event Implementations:**

- ✅ PaymentReceived (finance module) - Already implements BikeRentalEvent
- 🔜 Future events (rental, equipment, customer modules) will implement BikeRentalEvent

**Impact:**

- Type-safe event handling across the system
- Clear contract for all domain events
- Component tests can reliably capture domain events
- Framework events (Spring lifecycle events) automatically filtered out
- Improved debugging with event type logging

**Why This Matters:**

- Prevents accidental event contract violations at compile-time
- Enables generic event processing infrastructure
- Simplifies component test event verification
- Provides living documentation of system event contracts
- Foundation for future event sourcing or event store implementation

## Recent Changes (2026-02-03)

### Tariff Module Implementation Complete (US-TR-001)
