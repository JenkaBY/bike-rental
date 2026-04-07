package com.github.jenkaby.bikerental.rental.infrastructure.eventlistener;

import com.github.jenkaby.bikerental.finance.CustomerFundDeposited;
import com.github.jenkaby.bikerental.rental.application.usecase.SettleDebtUseCase;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class DebtSettlementEventListener {

    private final RentalRepository rentalRepository;
    private final SettleDebtUseCase settleDebtUseCase;

    @ApplicationModuleListener
    public void onCustomerFundDeposited(CustomerFundDeposited event) {
        log.info("Received CustomerFundDeposited for customerId={}, transactionId={}",
                event.customerId(), event.transactionId());

        var customerRef = new CustomerRef(event.customerId());
        var debtRentals = rentalRepository.getCustomerDebtRentals(customerRef);

        if (debtRentals.isEmpty()) {
            log.debug("No DEBT rentals found for customerId={}", event.customerId());
            return;
        }

        log.info("Attempting to settle {} DEBT rental(s) for customerId={}", debtRentals.size(), event.customerId());

        for (var rental : debtRentals) {
            var command = new SettleDebtUseCase.SettleDebtCommand(
                    customerRef,
                    RentalRef.of(rental.getId()),
                    event.operatorId()
            );
            var result = settleDebtUseCase.execute(command);
            if (!result.settled()) {
                return;
            }
        }
    }
}
