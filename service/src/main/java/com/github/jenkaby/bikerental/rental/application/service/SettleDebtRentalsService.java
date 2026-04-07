package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.usecase.SettleDebtUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.OverBudgetSettlementException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
class SettleDebtRentalsService implements SettleDebtUseCase {

    private final RentalRepository rentalRepository;
    private final FinanceFacade financeFacade;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SettleDebtResult execute(SettleDebtCommand command) {
        Rental rental = rentalRepository.findById(command.rentalRef().id())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalRef().id().toString()));

        try {
            financeFacade.settleRental(
                    command.customerRef(),
                    command.rentalRef(),
                    rental.getFinalCost(),
                    command.operatorId()
            );
            rental.completeForDebt();
            rentalRepository.save(rental);
            log.info("DEBT rental {} settled successfully for customer {}",
                    command.rentalRef().id(), command.customerRef().id());
            return SettleDebtResult.success();
        } catch (OverBudgetSettlementException e) {
            log.warn("DEBT rental {} could not be settled — insufficient funds {} for customer {}",
                    command.rentalRef().id(), e.getDetails().availableAmount(), command.customerRef().id());
            return SettleDebtResult.failure();
        }
    }
}
