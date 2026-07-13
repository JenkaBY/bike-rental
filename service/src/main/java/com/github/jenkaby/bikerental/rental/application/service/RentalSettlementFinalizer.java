package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.finance.SettlementInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase.ReturnEquipmentResult;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.OverBudgetSettlementException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Slf4j
@Component
@RequiredArgsConstructor
class RentalSettlementFinalizer {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final FinanceFacade financeFacade;
    private final RentalEventMapper eventMapper;
    private final EventPublisher eventPublisher;

    ReturnEquipmentResult settleAndComplete(Rental rental, Money totalFinalCost, String operatorId, LocalDateTime returnTime) {
        SettlementInfo settlementInfo = null;
        try {
            settlementInfo = financeFacade.settleRental(
                    CustomerRef.of(rental.getCustomerId()),
                    RentalRef.of(rental.getId()),
                    totalFinalCost,
                    operatorId
            );
            rental.completeWithStatus(totalFinalCost, RentalStatus.COMPLETED);
        } catch (OverBudgetSettlementException obe) {
            rental.completeWithStatus(totalFinalCost, RentalStatus.DEBT);
        }

        Rental saved = rentalRepository.save(rental);

        RentalCompleted event = eventMapper.toRentalCompleted(saved, returnTime, totalFinalCost);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);

        return new ReturnEquipmentResult(saved, settlementInfo);
    }
}
