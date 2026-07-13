package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
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

    private final RentalRepository rentalRepository;
    private final RentalDurationCalculator durationCalculator;
    private final RentalCostPolicy costPolicy;
    private final RentalSettlementFinalizer settlementFinalizer;
    private final Clock clock;

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

        return settlementFinalizer.settleAndComplete(rental, totalFinalCost, command.operatorId(), returnTime);
    }

    private Rental findRental(ReturnEquipmentCommand command) {
        return rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));
    }
}
