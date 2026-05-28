## 1. Title

bikerent — Spring Boot modular monolith API for rental lifecycle, equipment catalog, customer management, tariffs, and
wallet-based finance settlement.

## 2. Summary

- Java 21 Spring Boot service with Spring Modulith module boundaries (`customer`, `equipment`, `rental`, `tariff`,
  `finance`, `shared`).
- Primary runtime style: synchronous HTTP APIs plus in-process domain events via Spring `ApplicationEventPublisher`.
- Rental flow supports draft initialization, lifecycle activation/cancellation, equipment return, and debt settlement
  retry after deposits.
- Finance flow implements double-entry ledgers (`wallet`, `on-hold`, `revenue`, payment-method subledgers) and
  idempotent transaction recording.
- Persistence stack: PostgreSQL + Spring Data JPA + Liquibase changelogs.

## 3. Projects and Folder Map

- PATH: `service/`
  PURPOSE: Main Spring Boot application module.
  ENTRY_FILES: `service/src/main/java/com/github/jenkaby/bikerental/BikeRentalApplication.java`

- PATH: `component-test/`
  PURPOSE: Component-level automated tests.
  ENTRY_FILES: `component-test/src/test/java/com/github/jenkaby/bikerental/componenttest/RunComponentTests.java`

- PATH: `docker/`
  PURPOSE: Local infrastructure and container composition.
  ENTRY_FILES: `docker/docker-compose.yaml`

- PATH: `service/src/main/resources/db/changelog/`
  PURPOSE: Liquibase schema and seed changelogs.
  ENTRY_FILES: `service/src/main/resources/db/changelog/db.changelog-master.xml`

- PATH: `.github/`
  PURPOSE: CI and repository automation.
  ENTRY_FILES: `NONE`

## 4. Components

COMPONENT_NAME: BikeRentalApplication
TYPE: Gateway
PURPOSE: Boots Spring Boot application and module scanning.
RESPONSIBILITIES:

- Starts the JVM application context.
- Enables `@Modulithic(sharedModules = "shared")`.
- Enables configuration properties scanning.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/BikeRentalApplication.java`
  CALLS:
- NONE
  CALLED_BY:
- JVM main launcher

COMPONENT_NAME: RentalCommandController
TYPE: API
PURPOSE: Exposes command endpoints for rental creation, update, return, and lifecycle transitions.
RESPONSIBILITIES:

- Handles `POST /api/rentals`, `PUT /api/rentals/{rentalId}`, `POST /api/rentals/return`,
  `PATCH /api/rentals/{rentalId}/lifecycles`.
- Maps HTTP payloads to use-case commands.
- Returns mapped rental/return responses.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandController.java`
  CALLS:
- CreateOrUpdateDraftRentalService — create or update rental drafts.
- UpdateRentalService — apply JSON patch updates.
- ReturnEquipmentService — process equipment return.
- RentalLifecycleService — execute lifecycle transitions.
  CALLED_BY:
- External HTTP clients

COMPONENT_NAME: RentalQueryController
TYPE: API
PURPOSE: Exposes rental read endpoints.
RESPONSIBILITIES:

- Handles `GET /api/rentals/{id}`.
- Handles paged `GET /api/rentals` with filters.
- Maps domain page/results to response DTOs.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/web/query/RentalQueryController.java`
  CALLS:
- GetRentalByIdService — load one rental.
- FindRentalsService — load paginated rentals.
  CALLED_BY:
- External HTTP clients

COMPONENT_NAME: RentalAvailabilityQueryController
TYPE: API
PURPOSE: Exposes endpoint for equipment available for rent.
RESPONSIBILITIES:

- Handles `GET /api/rentals/available-equipments`.
- Maps query params to `EquipmentSearchFilter`.
- Returns paged availability results.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/web/query/RentalAvailabilityQueryController.java`
  CALLS:
- GetAvailableForRentEquipmentsService — compute available equipment list.
  CALLED_BY:
- External HTTP clients

COMPONENT_NAME: CreateOrUpdateDraftRentalService
TYPE: Service
PURPOSE: Creates rental drafts and updates draft rental content.
RESPONSIBILITIES:

- Validates customer and equipment references.
- Validates equipment condition/availability.
- Applies planned duration and optional special pricing.
- Persists draft rentals.
- Publishes `RentalCreated` on empty draft creation.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CreateOrUpdateDraftRentalService.java`
  CALLS:
- RentalRepositoryAdapter — read/save rentals.
- CustomerFacadeImpl — resolve customer existence.
- EquipmentFacadeImpl — resolve equipment info.
- RentalCostPolicy — recompute estimated cost.
- SpringApplicationEventPublisher — publish rental event.
  CALLED_BY:
- RentalCommandController

COMPONENT_NAME: UpdateRentalService
TYPE: Service
PURPOSE: Applies JSON-patch style updates to an existing rental.
RESPONSIBILITIES:

- Parses patch fields (`customerId`, `duration`, `equipmentIds`).
- Validates references and equipment availability.
- Replaces rental equipment set and emits `RentalUpdated`.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/UpdateRentalService.java`
  CALLS:
- RentalRepositoryAdapter — read/save rentals.
- CustomerFacadeImpl — validate patched customer.
- EquipmentFacadeImpl — load patched equipment set.
- SpringApplicationEventPublisher — publish rental update event.
  CALLED_BY:
- RentalCommandController

COMPONENT_NAME: RentalLifecycleService
TYPE: Service
PURPOSE: Routes lifecycle commands to activate/cancel handlers.
RESPONSIBILITIES:

- Dispatches `ACTIVE` target to activate service.
- Dispatches `CANCELLED` target to cancel service.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/RentalLifecycleService.java`
  CALLS:
- ActivateRentalService — activate rental.
- CancelRentalService — cancel rental.
  CALLED_BY:
- RentalCommandController

COMPONENT_NAME: ActivateRentalService
TYPE: Service
PURPOSE: Activates rental and optionally holds planned funds.
RESPONSIBILITIES:

- Validates transition to `ACTIVE`.
- Calls finance hold when estimated cost is positive.
- Sets `startedAt` and saves rental.
- Publishes `RentalStarted`.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ActivateRentalService.java`
  CALLS:
- RentalRepositoryAdapter — read/save rental aggregate.
- FinanceFacadeImpl — place hold transaction.
- SpringApplicationEventPublisher — publish lifecycle event.
  CALLED_BY:
- RentalLifecycleService

COMPONENT_NAME: CancelRentalService
TYPE: Service
PURPOSE: Cancels rental and releases hold for active rentals.
RESPONSIBILITIES:

- Validates transition to `CANCELLED`.
- Calls hold release when canceling active rental.
- Saves canceled rental and publishes event.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/CancelRentalService.java`
  CALLS:
- RentalRepositoryAdapter — read/save rental aggregate.
- FinanceFacadeImpl — release held funds.
- SpringApplicationEventPublisher — publish cancellation event.
  CALLED_BY:
- RentalLifecycleService

COMPONENT_NAME: ReturnEquipmentService
TYPE: Service
PURPOSE: Processes rental return and financial settlement.
RESPONSIBILITIES:

- Validates rental status and return target equipment.
- Calculates final cost using tariff facade.
- Settles finance and marks COMPLETED or DEBT.
- Persists updated rental and publishes `RentalCompleted`.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`
  CALLS:
- RentalRepositoryAdapter — read/save rental aggregate.
- EquipmentFacadeImpl — load returned equipment metadata.
- RentalCostPolicy — compute final costs.
- FinanceFacadeImpl — settle final rental amount.
- SpringApplicationEventPublisher — publish completion event.
  CALLED_BY:
- RentalCommandController

COMPONENT_NAME: SettleDebtRentalsService
TYPE: Service
PURPOSE: Attempts settlement of rentals in `DEBT` status.
RESPONSIBILITIES:

- Loads DEBT rental by id.
- Calls finance settlement against final rental cost.
- Marks rental completed-for-debt when settlement succeeds.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/SettleDebtRentalsService.java`
  CALLS:
- RentalRepositoryAdapter — load/save DEBT rentals.
- FinanceFacadeImpl — attempt settlement.
  CALLED_BY:
- DebtSettlementEventListener

COMPONENT_NAME: FindRentalsService
TYPE: Service
PURPOSE: Fetches paginated rentals by search filter.
RESPONSIBILITIES:

- Validates date range.
- Builds `RentalSearchFilter` and delegates repository query.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/FindRentalsService.java`
  CALLS:
- RentalRepositoryAdapter — execute filtered page query.
  CALLED_BY:
- RentalQueryController

COMPONENT_NAME: GetRentalByIdService
TYPE: Service
PURPOSE: Retrieves one rental by id.
RESPONSIBILITIES:

- Loads rental by id.
- Throws `ResourceNotFoundException` when absent.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/GetRentalByIdService.java`
  CALLS:
- RentalRepositoryAdapter — fetch rental by id.
  CALLED_BY:
- RentalQueryController

COMPONENT_NAME: GetAvailableForRentEquipmentsService
TYPE: Service
PURPOSE: Computes equipment list available for new rentals.
RESPONSIBILITIES:

- Fetches GOOD-condition equipment candidates.
- Excludes occupied equipment ids.
- Applies page slicing in-memory.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/GetAvailableForRentEquipmentsService.java`
  CALLS:
- EquipmentFacadeImpl — fetch candidate equipment pool.
- EquipmentAvailabilityService — resolve occupied ids.
  CALLED_BY:
- RentalAvailabilityQueryController

COMPONENT_NAME: EquipmentAvailabilityService
TYPE: Service
PURPOSE: Resolves occupied equipment ids from rental-equipment table.
RESPONSIBILITIES:

- Accepts candidate equipment ids.
- Returns ids with occupied rental-equipment statuses.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/EquipmentAvailabilityService.java`
  CALLS:
- RentalEquipmentRepositoryAdapter — query occupied ids.
  CALLED_BY:
- GetAvailableForRentEquipmentsService

COMPONENT_NAME: RentalCostPolicy
TYPE: Service
PURPOSE: Applies estimated and final cost calculation policies to rental aggregate.
RESPONSIBILITIES:

- Recomputes estimated rental cost.
- Recomputes final rental cost for return.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/RentalCostPolicy.java`
  CALLS:
- RentalEquipmentCostCalculator — compute per-equipment cost outputs.
- EquipmentFacadeImpl — load equipment info for estimated recalculation.
  CALLED_BY:
- CreateOrUpdateDraftRentalService
- ReturnEquipmentService

COMPONENT_NAME: RentalEquipmentCostCalculator
TYPE: Service
PURPOSE: Converts rental+equipment data to tariff cost commands and extracts per-item results.
RESPONSIBILITIES:

- Builds tariff calculation command.
- Calls tariff module calculator facade.
- Converts breakdown response to `EquipmentCostResult` list.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/RentalEquipmentCostCalculator.java`
  CALLS:
- TariffV2FacadeImpl — calculate estimated/final cost batches.
  CALLED_BY:
- RentalCostPolicy

COMPONENT_NAME: RentalRepositoryAdapter
TYPE: Repository
PURPOSE: JPA adapter for rental aggregate queries and persistence.
RESPONSIBILITIES:

- Persists rental aggregate graph.
- Supports id lookup, filtered paging, and DEBT-rental fetch.
- Maps JPA entities to domain models.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/adapter/RentalRepositoryAdapter.java`
  CALLS:
- NONE
  CALLED_BY:
- CreateOrUpdateDraftRentalService
- UpdateRentalService
- ActivateRentalService
- CancelRentalService
- ReturnEquipmentService
- SettleDebtRentalsService
- FindRentalsService
- GetRentalByIdService
- DebtSettlementEventListener

COMPONENT_NAME: RentalEquipmentRepositoryAdapter
TYPE: Repository
PURPOSE: JPA adapter for occupied rental-equipment lookups.
RESPONSIBILITIES:

- Queries equipment ids by occupied statuses.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/adapter/RentalEquipmentRepositoryAdapter.java`
  CALLS:
- NONE
  CALLED_BY:
- EquipmentAvailabilityService

COMPONENT_NAME: CustomerFacadeImpl
TYPE: Service
PURPOSE: Cross-module customer lookup facade.
RESPONSIBILITIES:

- Resolves customer by UUID.
- Resolves customer by phone.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/customer/CustomerFacadeImpl.java`
  CALLS:
- NONE
  CALLED_BY:
- CreateOrUpdateDraftRentalService
- UpdateRentalService

COMPONENT_NAME: EquipmentFacadeImpl
TYPE: Service
PURPOSE: Cross-module equipment lookup facade.
RESPONSIBILITIES:

- Resolves equipment by id(s).
- Resolves equipment by condition + search filter.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/equipment/EquipmentFacadeImpl.java`
  CALLS:
- NONE
  CALLED_BY:
- CreateOrUpdateDraftRentalService
- UpdateRentalService
- ReturnEquipmentService
- GetAvailableForRentEquipmentsService
- RentalCostPolicy

COMPONENT_NAME: TariffV2FacadeImpl
TYPE: Service
PURPOSE: Cross-module tariff lookup and cost calculation facade.
RESPONSIBILITIES:

- Resolves tariff by id.
- Executes cost calculation and return-cost calculation commands.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/tariff/TariffV2FacadeImpl.java`
  CALLS:
- NONE
  CALLED_BY:
- RentalEquipmentCostCalculator

COMPONENT_NAME: FinanceFacadeImpl
TYPE: Service
PURPOSE: Cross-module finance operations facade for rental lifecycle.
RESPONSIBILITIES:

- Holds funds for rental activation.
- Settles rental against hold/wallet.
- Releases hold on cancellation.
- Checks hold existence for rental.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacadeImpl.java`
  CALLS:
- RecordRentalHoldService — create HOLD transaction.
- SettleRentalService — execute settlement capture/release logic.
- ReleaseHoldService — release held funds.
- TransactionRepositoryAdapter — `hasHold` lookup.
  CALLED_BY:
- ActivateRentalService
- CancelRentalService
- ReturnEquipmentService
- SettleDebtRentalsService

COMPONENT_NAME: RecordRentalHoldService
TYPE: Service
PURPOSE: Records HOLD transaction by moving funds wallet → on-hold.
RESPONSIBILITIES:

- Enforces idempotency for rental hold.
- Validates available customer balance.
- Persists account and transaction changes.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordRentalHoldService.java`
  CALLS:
- AccountRepositoryAdapter — load/save customer account.
- TransactionRepositoryAdapter — idempotency check and transaction write.
- UuidCreatorAdapter — generate ids/idempotency seed.
  CALLED_BY:
- FinanceFacadeImpl

COMPONENT_NAME: SettleRentalService
TYPE: Service
PURPOSE: Settles final rental cost using hold capture and optional wallet shortfall capture.
RESPONSIBILITIES:

- Loads existing hold/capture state.
- Captures hold to revenue and optionally releases excess.
- Captures wallet shortfall when required.
- Throws `OverBudgetSettlementException` when funds are insufficient.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/SettleRentalService.java`
  CALLS:
- AccountRepositoryAdapter — load/save system and customer accounts.
- TransactionRepositoryAdapter — read/write hold/capture/release transactions.
- UuidCreatorAdapter — generate transaction identifiers.
  CALLED_BY:
- FinanceFacadeImpl

COMPONENT_NAME: ReleaseHoldService
TYPE: Service
PURPOSE: Releases held rental funds back to wallet.
RESPONSIBILITIES:

- Enforces idempotent release per rental.
- Loads hold transaction and customer account.
- Records RELEASE transaction and account updates.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/ReleaseHoldService.java`
  CALLS:
- AccountRepositoryAdapter — load/save customer account.
- TransactionRepositoryAdapter — locate hold and save release.
- UuidCreatorAdapter — generate idempotency/transaction ids.
  CALLED_BY:
- FinanceFacadeImpl

COMPONENT_NAME: DepositCommandController
TYPE: API
PURPOSE: Exposes endpoint to record wallet deposits.
RESPONSIBILITIES:

- Handles `POST /api/finance/deposits`.
- Maps request to deposit use-case command.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/DepositCommandController.java`
  CALLS:
- RecordDepositService — execute deposit posting.
  CALLED_BY:
- External HTTP clients

COMPONENT_NAME: WithdrawalCommandController
TYPE: API
PURPOSE: Exposes endpoint to record wallet withdrawals.
RESPONSIBILITIES:

- Handles `POST /api/finance/withdrawals`.
- Maps request to withdrawal use-case command.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/WithdrawalCommandController.java`
  CALLS:
- RecordWithdrawalService — execute withdrawal posting.
  CALLED_BY:
- External HTTP clients

COMPONENT_NAME: AdjustmentCommandController
TYPE: API
PURPOSE: Exposes endpoint for manual balance adjustments.
RESPONSIBILITIES:

- Handles `POST /api/finance/adjustments`.
- Maps request to adjustment use-case command.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/AdjustmentCommandController.java`
  CALLS:
- ApplyAdjustmentService — execute adjustment posting.
  CALLED_BY:
- External HTTP clients

COMPONENT_NAME: AccountQueryController
TYPE: API
PURPOSE: Exposes finance account balances and transaction history endpoints.
RESPONSIBILITIES:

- Handles `GET /api/finance/customers/{customerId}/balances`.
- Handles `GET /api/finance/customers/{customerId}/transactions`.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/web/query/AccountQueryController.java`
  CALLS:
- GetCustomerAccountBalancesService — load wallet/on-hold balances.
- GetTransactionHistoryService — load paged transaction history.
  CALLED_BY:
- External HTTP clients

COMPONENT_NAME: RecordDepositService
TYPE: Service
PURPOSE: Records customer deposits and emits deposit domain event.
RESPONSIBILITIES:

- Enforces deposit idempotency.
- Posts ledger entries system payment-method → customer wallet.
- Persists transaction and publishes `CustomerFundDeposited`.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordDepositService.java`
  CALLS:
- AccountRepositoryAdapter — load/save system and customer accounts.
- TransactionRepositoryAdapter — idempotency check and write.
- UuidCreatorAdapter — generate transaction and record ids.
- SpringApplicationEventPublisher — publish finance event.
  CALLED_BY:
- DepositCommandController

COMPONENT_NAME: RecordWithdrawalService
TYPE: Service
PURPOSE: Records customer withdrawals.
RESPONSIBILITIES:

- Enforces withdrawal idempotency.
- Validates available balance.
- Posts ledger entries customer wallet → system payment-method.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordWithdrawalService.java`
  CALLS:
- AccountRepositoryAdapter — load/save system and customer accounts.
- TransactionRepositoryAdapter — idempotency check and write.
- UuidCreatorAdapter — generate transaction and record ids.
  CALLED_BY:
- WithdrawalCommandController

COMPONENT_NAME: ApplyAdjustmentService
TYPE: Service
PURPOSE: Applies signed manual adjustments to customer wallet.
RESPONSIBILITIES:

- Enforces adjustment idempotency.
- Handles debit/credit direction by sign of amount.
- Persists adjustment transaction records.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/ApplyAdjustmentService.java`
  CALLS:
- AccountRepositoryAdapter — load/save system and customer accounts.
- TransactionRepositoryAdapter — idempotency check and write.
- UuidCreatorAdapter — generate transaction and record ids.
  CALLED_BY:
- AdjustmentCommandController

COMPONENT_NAME: GetCustomerAccountBalancesService
TYPE: Service
PURPOSE: Returns wallet and hold balances for a customer account.
RESPONSIBILITIES:

- Loads customer account by customer id.
- Returns wallet balance, hold balance, and update timestamp.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/GetCustomerAccountBalancesService.java`
  CALLS:
- AccountRepositoryAdapter — load customer account.
  CALLED_BY:
- AccountQueryController

COMPONENT_NAME: GetTransactionHistoryService
TYPE: Service
PURPOSE: Returns paginated customer transaction history.
RESPONSIBILITIES:

- Validates account existence.
- Queries filtered transactions.
- Maps transactions to DTO page.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/GetTransactionHistoryService.java`
  CALLS:
- AccountRepositoryAdapter — validate account exists.
- TransactionRepositoryAdapter — read filtered transaction history.
  CALLED_BY:
- AccountQueryController

COMPONENT_NAME: CustomerCommandController
TYPE: API
PURPOSE: Exposes customer create and update endpoints.
RESPONSIBILITIES:

- Handles `POST /api/customers`.
- Handles `PUT /api/customers/{id}`.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/customer/web/command/CustomerCommandController.java`
  CALLS:
- CreateCustomerService — create customer record.
  CALLED_BY:
- External HTTP clients

COMPONENT_NAME: CreateCustomerService
TYPE: Service
PURPOSE: Persists customers and emits registration event.
RESPONSIBILITIES:

- Validates duplicate phone.
- Saves new customer entity.
- Publishes `CustomerRegistered` event.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/customer/application/service/CreateCustomerService.java`
  CALLS:
- SpringApplicationEventPublisher — publish customer registration event.
  CALLED_BY:
- CustomerCommandController

COMPONENT_NAME: FinanceCustomerEventListener
TYPE: Consumer
PURPOSE: Creates finance customer account on customer registration.
RESPONSIBILITIES:

- Consumes `CustomerRegistered` before transaction commit.
- Delegates account creation to finance use-case service.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/eventlistener/FinanceCustomerEventListener.java`
  CALLS:
- CreateCustomerAccountService — create wallet and hold subledgers.
  CALLED_BY:
- SpringApplicationEventPublisher

COMPONENT_NAME: CreateCustomerAccountService
TYPE: Service
PURPOSE: Initializes finance account for a new customer.
RESPONSIBILITIES:

- Ensures account does not already exist.
- Creates wallet and on-hold subledgers.
- Saves new customer account.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/CreateCustomerAccountService.java`
  CALLS:
- AccountRepositoryAdapter — existence check and save account.
- UuidCreatorAdapter — generate account/subledger ids.
  CALLED_BY:
- FinanceCustomerEventListener

COMPONENT_NAME: DebtSettlementEventListener
TYPE: Consumer
PURPOSE: Retries settlement of DEBT rentals when customer deposits funds.
RESPONSIBILITIES:

- Consumes `CustomerFundDeposited`.
- Loads customer DEBT rentals ordered by created time.
- Calls debt settlement service until first failure.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/eventlistener/DebtSettlementEventListener.java`
  CALLS:
- RentalRepositoryAdapter — load DEBT rentals for customer.
- SettleDebtRentalsService — settle each DEBT rental.
  CALLED_BY:
- SpringApplicationEventPublisher

COMPONENT_NAME: SpringApplicationEventPublisher
TYPE: Producer
PURPOSE: Publishes in-process application events through Spring event bus.
RESPONSIBILITIES:

- Validates destination/message arguments.
- Emits domain events through `ApplicationEventPublisher`.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/shared/infrastructure/messaging/SpringApplicationEventPublisher.java`
  CALLS:
- NONE
  CALLED_BY:
- CreateOrUpdateDraftRentalService
- UpdateRentalService
- ActivateRentalService
- CancelRentalService
- ReturnEquipmentService
- CreateCustomerService
- RecordDepositService

COMPONENT_NAME: AccountRepositoryAdapter
TYPE: Repository
PURPOSE: JPA adapter for finance account aggregate.
RESPONSIBILITIES:

- Saves customer/system accounts.
- Retrieves system account.
- Retrieves customer account by customer id.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/adapter/AccountRepositoryAdapter.java`
  CALLS:
- NONE
  CALLED_BY:
- RecordRentalHoldService
- SettleRentalService
- ReleaseHoldService
- RecordDepositService
- RecordWithdrawalService
- ApplyAdjustmentService
- GetCustomerAccountBalancesService
- GetTransactionHistoryService
- CreateCustomerAccountService

COMPONENT_NAME: TransactionRepositoryAdapter
TYPE: Repository
PURPOSE: JPA adapter for finance transaction persistence and search.
RESPONSIBILITIES:

- Persists transactions.
- Supports idempotency/rental transaction lookups.
- Supports paginated transaction history queries.
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/adapter/TransactionRepositoryAdapter.java`
  CALLS:
- NONE
  CALLED_BY:
- FinanceFacadeImpl
- RecordRentalHoldService
- SettleRentalService
- ReleaseHoldService
- RecordDepositService
- RecordWithdrawalService
- ApplyAdjustmentService
- GetTransactionHistoryService

COMPONENT_NAME: CoreExceptionHandlerAdvice
TYPE: Utility
PURPOSE: Global exception mapping to RFC7807 `ProblemDetail` responses.
RESPONSIBILITIES:

- Maps validation, domain, and generic errors to HTTP statuses.
- Adds `correlationId` and error codes to response body.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/shared/web/advice/CoreExceptionHandlerAdvice.java`
  CALLS:
- UuidCreatorAdapter — generate fallback correlation id.
  CALLED_BY:
- Spring MVC exception pipeline

COMPONENT_NAME: CorrelationIdFilter
TYPE: Utility
PURPOSE: Sets request correlation id in MDC and response header.
RESPONSIBILITIES:

- Reads `X-Correlation-ID` or generates UUID.
- Adds/removes MDC value for request thread.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/shared/web/filter/CorrelationIdFilter.java`
  CALLS:
- NONE
  CALLED_BY:
- Spring servlet filter chain

COMPONENT_NAME: UuidCreatorAdapter
TYPE: Utility
PURPOSE: Provides UUID generation port implementation.
RESPONSIBILITIES:

- Generates time-ordered UUID values.
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/shared/infrastructure/port/uuid/UuidCreatorAdapter.java`
  CALLS:
- NONE
  CALLED_BY:
- RecordRentalHoldService
- SettleRentalService
- ReleaseHoldService
- RecordDepositService
- RecordWithdrawalService
- ApplyAdjustmentService
- CreateCustomerAccountService
- CoreExceptionHandlerAdvice

## 5. Component Call Sequences

### Use-Case: Activate Rental With Hold

STEP 1: RentalCommandController → RentalLifecycleService
OPERATION: execute(RentalLifecycleCommand target=ACTIVE)
PURPOSE: Route lifecycle request from API to activation branch.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandController.java`

STEP 2: RentalLifecycleService → ActivateRentalService
OPERATION: execute(ActivateCommand)
PURPOSE: Execute ACTIVE transition workflow.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/RentalLifecycleService.java`

STEP 3: ActivateRentalService → RentalRepositoryAdapter
OPERATION: findById(rentalId)
PURPOSE: Load rental aggregate before transition.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ActivateRentalService.java`

STEP 4: ActivateRentalService → FinanceFacadeImpl
OPERATION: holdFunds(customerRef, rentalRef, estimatedCost, operatorId)
PURPOSE: Move estimated funds to HOLD before start.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ActivateRentalService.java`

STEP 5: FinanceFacadeImpl → RecordRentalHoldService
OPERATION: execute(RentalHoldCommand)
PURPOSE: Create HOLD transaction and update account ledgers.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacadeImpl.java`

STEP 6: RecordRentalHoldService → AccountRepositoryAdapter
OPERATION: findByCustomerId(...) + save(customerAccount)
PURPOSE: Persist wallet/on-hold balance changes.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordRentalHoldService.java`

STEP 7: RecordRentalHoldService → TransactionRepositoryAdapter
OPERATION: save(HOLD transaction)
PURPOSE: Persist HOLD transaction journal entry.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordRentalHoldService.java`

STEP 8: ActivateRentalService → RentalRepositoryAdapter
OPERATION: save(rental)
PURPOSE: Persist ACTIVE status and start timestamp.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ActivateRentalService.java`

STEP 9: ActivateRentalService → SpringApplicationEventPublisher
OPERATION: publish("rental-events", RentalStarted)
PURPOSE: Emit lifecycle event for downstream consumers.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ActivateRentalService.java`

### Use-Case: Return Equipment And Retry Debt Settlement

STEP 1: RentalCommandController → ReturnEquipmentService
OPERATION: execute(ReturnEquipmentCommand)
PURPOSE: Start return and final settlement workflow.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandController.java`

STEP 2: ReturnEquipmentService → RentalRepositoryAdapter
OPERATION: findById(rentalId)
PURPOSE: Load active rental aggregate.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`

STEP 3: ReturnEquipmentService → RentalCostPolicy
OPERATION: calculateFinalCost(rental, returningEquipments, billableDuration)
PURPOSE: Compute final payable amount.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`

STEP 4: RentalCostPolicy → RentalEquipmentCostCalculator
OPERATION: calculateFinal(...)
PURPOSE: Build tariff command and per-equipment breakdown.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/RentalCostPolicy.java`

STEP 5: RentalEquipmentCostCalculator → TariffV2FacadeImpl
OPERATION: calculateRentalCost(command)
PURPOSE: Execute tariff cost calculation.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/RentalEquipmentCostCalculator.java`

STEP 6: ReturnEquipmentService → FinanceFacadeImpl
OPERATION: settleRental(customerRef, rentalRef, totalFinalCost, operatorId)
PURPOSE: Capture/release held funds and finalize payment.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`

STEP 7: FinanceFacadeImpl → SettleRentalService
OPERATION: execute(SettleRentalCommand)
PURPOSE: Apply settlement algorithm (CAPTURE/RELEASE/shortfall).
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/FinanceFacadeImpl.java`

STEP 8: SettleRentalService → TransactionRepositoryAdapter
OPERATION: save(CAPTURE/RELEASE transactions)
PURPOSE: Persist settlement journal entries.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/SettleRentalService.java`

STEP 9: ReturnEquipmentService → SpringApplicationEventPublisher
OPERATION: publish("rental-events", RentalCompleted)
PURPOSE: Emit completion event after rental save.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/application/service/ReturnEquipmentService.java`

STEP 10: DepositCommandController → RecordDepositService
OPERATION: execute(RecordDepositCommand)
PURPOSE: Add customer funds when debt is later paid.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/DepositCommandController.java`

STEP 11: RecordDepositService → SpringApplicationEventPublisher
OPERATION: publish("finance-events", CustomerFundDeposited)
PURPOSE: Trigger DEBT rental retry after deposit.
SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/application/service/RecordDepositService.java`

STEP 12: DebtSettlementEventListener → SettleDebtRentalsService
OPERATION: execute(SettleDebtCommand)
PURPOSE: Retry settlement of oldest DEBT rentals.
SOURCE:
`service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/eventlistener/DebtSettlementEventListener.java`

## 6. Communication Channels

- CHANNEL_TYPE: HTTP
  ENDPOINT / EXCHANGE / TOPIC: `PUT /api/rentals/{rentalId}`, `POST /api/rentals`, `POST /api/rentals/return`,
  `PATCH /api/rentals/{rentalId}/lifecycles`
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/web/command/RentalCommandController.java`
  NOTES: JSON API, ProblemDetail error contract.

- CHANNEL_TYPE: HTTP
  ENDPOINT / EXCHANGE / TOPIC: `GET /api/rentals`, `GET /api/rentals/{id}`, `GET /api/rentals/available-equipments`
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/rental/web/query/RentalQueryController.java`,
  `service/src/main/java/com/github/jenkaby/bikerental/rental/web/query/RentalAvailabilityQueryController.java`
  NOTES: Pageable query endpoints.

- CHANNEL_TYPE: HTTP
  ENDPOINT / EXCHANGE / TOPIC: `POST /api/finance/deposits`, `POST /api/finance/withdrawals`,
  `POST /api/finance/adjustments`
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/web/command/*.java`
  NOTES: Wallet command endpoints; idempotency key expected in request payload.

- CHANNEL_TYPE: HTTP
  ENDPOINT / EXCHANGE / TOPIC: `GET /api/finance/customers/{customerId}/balances`,
  `GET /api/finance/customers/{customerId}/transactions`
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/finance/web/query/AccountQueryController.java`
  NOTES: Customer account read endpoints.

- CHANNEL_TYPE: HTTP
  ENDPOINT / EXCHANGE / TOPIC: `/api/customers`, `/api/equipments`, `/api/tariffs`
  SOURCE: `service/src/main/java/com/github/jenkaby/bikerental/customer/web`,
  `service/src/main/java/com/github/jenkaby/bikerental/equipment/web`,
  `service/src/main/java/com/github/jenkaby/bikerental/tariff/web`
  NOTES: CRUD and query APIs for supporting modules.

- CHANNEL_TYPE: HTTP
  ENDPOINT / EXCHANGE / TOPIC: `/actuator/health`, `/actuator/info`, `/actuator/metrics`
  SOURCE: `service/src/main/resources/application-management-config.yaml`
  NOTES: Spring Actuator management endpoints.

- CHANNEL_TYPE: MessageQueue
  ENDPOINT / EXCHANGE / TOPIC: `customer-events`, `rental-events`, `finance-events`
  SOURCE:
  `service/src/main/java/com/github/jenkaby/bikerental/shared/infrastructure/messaging/SpringApplicationEventPublisher.java`
  NOTES: In-process Spring event bus, not external broker.

- CHANNEL_TYPE: gRPC
  ENDPOINT / EXCHANGE / TOPIC: NONE
  SOURCE: NONE
  NOTES: NONE

- CHANNEL_TYPE: Webhook
  ENDPOINT / EXCHANGE / TOPIC: NONE
  SOURCE: NONE
  NOTES: NONE

## 7. Dependency Registration and Wiring

- DI_CONTAINER: Spring IoC container (Spring Boot component scanning + `@Configuration` beans)
- REGISTRATION_FILE: `service/src/main/java/com/github/jenkaby/bikerental/BikeRentalApplication.java` (
  `@SpringBootApplication`), plus stereotype annotations in module packages

- REGISTRATION:
  - LIFETIME/SCOPE: Singleton
  - ABSTRACTION: EventPublisher
  - CONCRETE: SpringApplicationEventPublisher
  - LOCATION:
    `service/src/main/java/com/github/jenkaby/bikerental/shared/infrastructure/messaging/SpringApplicationEventPublisher.java`
  - SNIPPET:
    ```java
    @Component
    public class SpringApplicationEventPublisher implements EventPublisher {
        private final ApplicationEventPublisher eventPublisher;
    }
    ```

- REGISTRATION:
  - LIFETIME/SCOPE: Singleton
  - ABSTRACTION: UuidGenerator
  - CONCRETE: UuidCreatorAdapter
  - LOCATION:
    `service/src/main/java/com/github/jenkaby/bikerental/shared/infrastructure/port/uuid/UuidCreatorAdapter.java`
  - SNIPPET:
    ```java
    @Component
    public class UuidCreatorAdapter implements UuidGenerator {
        public UUID generate() { return UuidCreator.getTimeOrderedEpoch(); }
    }
    ```

- REGISTRATION:
  - LIFETIME/SCOPE: Singleton
  - ABSTRACTION: Clock
  - CONCRETE: `Clock.systemDefaultZone()`
  - LOCATION: `service/src/main/java/com/github/jenkaby/bikerental/shared/config/ClockConfig.java#applicationClock`
  - SNIPPET:
    ```java
    @Configuration
    public class ClockConfig {
        @Bean
        public Clock applicationClock() { return Clock.systemDefaultZone(); }
    }
    ```

- REGISTRATION:
  - LIFETIME/SCOPE: Singleton
  - ABSTRACTION: CorsConfigurationSource
  - CONCRETE: UrlBasedCorsConfigurationSource
  - LOCATION:
    `service/src/main/java/com/github/jenkaby/bikerental/shared/config/CorsConfig.java#corsConfigurationSource`
  - SNIPPET:
    ```java
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    ```

## 8. Configuration and Secrets

- SOURCE_TYPE: config file
  KEYS: `spring.application.name`, `spring.jpa.open-in-view`, `spring.liquibase.change-log`,
  `app.rental.time-increment`, `app.rental.forgiveness.overtime-duration`, `app.dev.virtual-clock.sse-interval-ms`
  SENSITIVE: NO
  LOCATION: `service/src/main/resources/application.yaml`

- SOURCE_TYPE: environment variable
  KEYS: `DATASOURCE_URL`, `DATASOURCE_USER`, `DATASOURCE_SECRET`
  SENSITIVE: YES
  LOCATION: `service/src/main/resources/application.yaml`

- SOURCE_TYPE: environment variable
  KEYS: `CORS_ALLOWED_ORIGINS`
  SENSITIVE: NO
  LOCATION: `service/src/main/resources/application.yaml`

- SOURCE_TYPE: config file
  KEYS: `management.endpoints.web.exposure.include`, `management.endpoint.health.show-details`,
  `management.info.env.enabled`
  SENSITIVE: NO
  LOCATION: `service/src/main/resources/application-management-config.yaml`

- SOURCE_TYPE: secrets manager
  KEYS: NONE
  SENSITIVE: NONE
  LOCATION: NONE

## 9. Persistence and Data Access

- DATABASE: PostgreSQL (`postgres:15-alpine` in compose)
- DATA_ACCESS: Spring Data JPA + Hibernate
- MIGRATIONS_PATH: `service/src/main/resources/db/changelog/`
- REPOSITORY_PATTERN: YES
  - ABSTRACTION: `RentalRepository`
    IMPLEMENTATION: `RentalRepositoryAdapter` (
    `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/adapter/RentalRepositoryAdapter.java`)
  - ABSTRACTION: `RentalEquipmentRepository`
    IMPLEMENTATION: `RentalEquipmentRepositoryAdapter` (
    `service/src/main/java/com/github/jenkaby/bikerental/rental/infrastructure/persistence/adapter/RentalEquipmentRepositoryAdapter.java`)
  - ABSTRACTION: `AccountRepository`
    IMPLEMENTATION: `AccountRepositoryAdapter` (
    `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/adapter/AccountRepositoryAdapter.java`)
  - ABSTRACTION: `TransactionRepository`
    IMPLEMENTATION: `TransactionRepositoryAdapter` (
    `service/src/main/java/com/github/jenkaby/bikerental/finance/infrastructure/persistence/adapter/TransactionRepositoryAdapter.java`)
  - ABSTRACTION: `CustomerRepository`
    IMPLEMENTATION: `CustomerRepositoryAdapter` (
    `service/src/main/java/com/github/jenkaby/bikerental/customer/infrastructure/persistence/adapter/CustomerRepositoryAdapter.java`)
  - ABSTRACTION: `EquipmentRepository`
    IMPLEMENTATION: `EquipmentRepositoryAdapter` (
    `service/src/main/java/com/github/jenkaby/bikerental/equipment/infrastructure/persistence/adapter/EquipmentRepositoryAdapter.java`)
  - ABSTRACTION: `TariffV2Repository`
    IMPLEMENTATION: `TariffV2RepositoryAdapter` (
    `service/src/main/java/com/github/jenkaby/bikerental/tariff/infrastructure/persistence/adapter/TariffV2RepositoryAdapter.java`)

## 10. Patterns and Architecture Notes

- PATTERN: Modular Monolith (Spring Modulith)
  EVIDENCE: `service/src/main/java/com/github/jenkaby/bikerental/BikeRentalApplication.java` (`@Modulithic`)
  SNIPPET:
  ```java
  @SpringBootApplication
  @Modulithic(sharedModules = "shared")
  @ConfigurationPropertiesScan
  public class BikeRentalApplication {}
  ```

- PATTERN: Ports and Adapters (Hexagonal)
  EVIDENCE: `rental/domain/repository/RentalRepository.java`,
  `rental/infrastructure/persistence/adapter/RentalRepositoryAdapter.java`
  SNIPPET:
  ```java
  @Repository
  class RentalRepositoryAdapter implements RentalRepository {
      public Rental save(Rental rental) { ... }
  }
  ```

- PATTERN: Repository Pattern
  EVIDENCE: `finance/domain/repository/TransactionRepository.java` +
  `finance/infrastructure/persistence/adapter/TransactionRepositoryAdapter.java`
  SNIPPET: NONE

- PATTERN: Event-Driven In-Process Integration
  EVIDENCE: `shared/infrastructure/messaging/SpringApplicationEventPublisher.java`,
  `rental/infrastructure/eventlistener/DebtSettlementEventListener.java`,
  `finance/infrastructure/eventlistener/FinanceCustomerEventListener.java`
  SNIPPET:
  ```java
  @ApplicationModuleListener
  public void onCustomerFundDeposited(CustomerFundDeposited event) {
      settleDebtUseCase.execute(command);
  }
  ```

- PATTERN: CQRS-style API split
  EVIDENCE: `rental/web/command/RentalCommandController.java`, `rental/web/query/RentalQueryController.java`
  SNIPPET: NONE

## 11. Security and Operational Considerations

- AUTHN_AUTHZ: NONE configured in active runtime; security dependencies are commented out in `service/build.gradle`.
- KNOWN_RISKS:
  - Public API endpoints without authentication/authorization gate.
  - `spring.liquibase.drop-first: true` in `service/src/main/resources/application.yaml` risks destructive schema reset
    if used outside controlled environments.
  - Docker compose includes plaintext development database credentials in `docker/docker-compose.yaml`.
- OBSERVABILITY:
  - Logging: Spring Boot logging (SLF4J/Logback), `CorrelationIdFilter` sets MDC correlation id.
  - Health/Metrics: Actuator endpoints configured in `service/src/main/resources/application-management-config.yaml`.
  - Error telemetry surface: `CoreExceptionHandlerAdvice` emits structured `ProblemDetail` with error code and
    correlation id.
- DEPLOYMENT:
  - `service/Dockerfile`
  - `docker/docker-compose.yaml`
  - `NONE` (Helm/Kubernetes manifests not found)

