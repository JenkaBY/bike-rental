package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.finance.SettlementInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import com.github.jenkaby.bikerental.shared.exception.OverBudgetSettlementException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
class ReturnEquipmentService implements ReturnEquipmentUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final RentalDurationCalculator durationCalculator;
    private final FinanceFacade financeFacade;
    private final RentalEventMapper eventMapper;
    private final EventPublisher eventPublisher;
    private final Clock clock;
    private final RentalCostPolicy costPolicy;

    @Override
    @Transactional
    public @NonNull ReturnEquipmentResult execute(@NonNull ReturnEquipmentCommand command) {
        log.info("Processing equipment return for rentalId={}, equipmentIds={}, equipmentUids={}",
                command.rentalId(), command.equipmentIds(), command.equipmentUids());

        LocalDateTime returnTime = LocalDateTime.now(clock);
        Rental rental = findRental(command);
        if (!rental.hasActiveStatus()) {
            throw new InvalidRentalStatusException(rental.getStatus(), RentalStatus.ACTIVE);
        }

        rental.calculateActualDuration(durationCalculator, returnTime);
        var equipmentsToReturn = rental.equipmentsToReturn(command.getEquipmentIds(), command.getEquipmentUids(), returnTime);

        costPolicy.calculateFinalCost(rental, equipmentsToReturn, durationCalculator, returnTime);

        if (!rental.allEquipmentsReturned()) {
            Rental saved = rentalRepository.save(rental);
            log.info("Partial return recorded for rental {}", saved.getId());
            return new ReturnEquipmentResult(saved, null);
        }

        var totalFinalCost = rental.getFinalCost();
        log.info("Rental [{}] returning equipments {}, final cost [{}]", rental.getId(), equipmentsToReturn, totalFinalCost);
        SettlementInfo settlementInfo = null;
        try {
            settlementInfo = financeFacade.settleRental(
                    CustomerRef.of(rental.getCustomerId()),
                    RentalRef.of(rental.getId()),
                    totalFinalCost,
                    command.operatorId()
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

    private Rental findRental(ReturnEquipmentCommand command) {
        return rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));
    }
}
