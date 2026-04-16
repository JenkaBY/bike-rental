- [x] Integrate Tariff v2 into rental system
- [x] Integrate hold funds (FinanceFacade) into rental module
- [x] Remove Tariff v1
- [x] Remove Finance Facade v1
- [x] Close rental debt. Possible solution is to fire events customer debit wallet and handle it in the rental module
- [] Remove 'expectedReturnAt', 'actualReturnedAt' (RentalReturnResponse, other places). Use startedAt + durationMinutes 
- [] Refactor EventListeners and move the logic to outside the listeners.
- [] Rental request should accept duration in min, not Java duration
- [] PATCH Rental request. Replace by several individual endpoints 
- [] Update Rental model to get rid of the estimatedCost and final cost fields. They might be persisted and returned as
  response but model must calculate them based on fields
- [] Review CalculationBreakdownMapper, BatchCalculationMapper and their location
- [] Create a new RentalPartialReturnedEvent event. Revise RentalCompletedEvent, probably we don't need a cost there
- [] Add discount and special tariff to Rental response
- [] Handle case when special price is 0
- [] [Optional] Add ability to set up special price and discount during return equipments. Now discount and special
  tariff can be applied during creation rental only
- [x] use '/api/tariffs' tariff endpoints for V2