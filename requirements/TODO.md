- [ ] Fix Tariff module to calculate rental equipment item individually with own returned at time. Persist estimated
  cost and breakdown to DB
- [ ] Fix Tariff module for flat tariffs to calculate cost based on returning dates. For ex. Helmet is rented
  yesterday's evening and returning the next day morning, customer should be charged as `2d*flat price`, now it depends
  on actual duration put in day term
- [ ] Draft rental must accept the same payload as update PUT rental.
- [ ] Remove Rental statuses endpoints and downstream code.
- [ ] Remove 'expectedReturnAt', 'actualReturnedAt' (RentalReturnResponse, other places). Use startedAt +
  durationMinutes
- [ ] Refactor EventListeners and move the logic to outside the listeners.
- [ ] PATCH Rental request. Replace by several individual endpoints
- [ ] Review CalculationBreakdownMapper, BatchCalculationMapper and their location
- [ ] Create a new RentalPartialReturnedEvent event. Revise RentalCompletedEvent, probably we don't need a cost there
- [ ] Add discount and special tariff to Rental response
- [ ] Handle case when special price is 0
- [ ] [Optional] Add ability to set up special price and discount during return equipments. Now discount and special
  tariff can be applied during creation rental only
- [ ] Create separate use cases for customer scenarios ???
- [ ] Use the same email validator for both request and EmailAddress

--- 

- [x] Rental request should accept duration in min, not Java duration
- [x] use '/api/tariffs' tariff endpoints for V2
- [x] Integrate Tariff v2 into rental system
- [x] Integrate hold funds (FinanceFacade) into rental module
- [x] Remove Tariff v1
- [x] Remove Finance Facade v1
- [x] Close rental debt. Possible solution is to fire events customer debit wallet and handle it in the rental module
