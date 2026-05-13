package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.finance.SettlementInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalCostCommandMapper;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import com.github.jenkaby.bikerental.shared.exception.OverBudgetSettlementException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.tariff.TariffV2Facade;
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
    private final TariffV2Facade tariffV2Facade;
    private final RentalCostCommandMapper costCommandMapper;
    private final FinanceFacade financeFacade;
    private final RentalEventMapper eventMapper;
    private final EventPublisher eventPublisher;
    private final EquipmentFacade equipmentFacade;
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

//        var durationResult = rental.calculateActualDuration(durationCalculator, returnTime);
//        var equipmentsToReturn = rental.equipmentsToReturn(command.getEquipmentIds(), command.getEquipmentUids(), returnTime);
//
//        var costCommand = costCommandMapper.toReturnCommand(rental, equipmentsToReturn, durationResult.billableDuration());
//        var costResult = tariffV2Facade.calculateRentalCost(costCommand);
//
//        var breakdowns = costResult.equipmentBreakdowns();
//        for (int i = 0; i < equipmentsToReturn.size(); i++) {
//            var equipment = equipmentsToReturn.get(i);
//            var breakdown = breakdowns.get(i);
//            equipment.setFinalCost(breakdown.itemCost());
//            equipment.setTariffId(breakdown.tariffId());
//        }

        var durationResult = rental.calculateActualDuration(durationCalculator, returnTime);
        var equipmentsToReturn = rental.equipmentsToReturn(command.getEquipmentIds(), command.getEquipmentUids(), returnTime);
        var equipmentInfos = equipmentFacade.findByIds(equipmentsToReturn.stream()
                .map(RentalEquipment::getEquipmentId)
                .toList());

        costPolicy.calculateFinalCost(rental, equipmentInfos, durationResult.billableDuration());

        if (!rental.allEquipmentsReturned()) {
            Rental saved = rentalRepository.save(rental);
            log.info("Partial return recorded for rental {}", saved.getId());
            return new ReturnEquipmentResult(saved, null);
        }

        // TODO Move to rental class
//        Money previouslyReturnedCost = rental.getEquipments().stream()
//                .filter(e -> e.getStatus() == RentalEquipmentStatus.RETURNED)
//                .filter(e -> !equipmentsToReturn.contains(e))
//                .map(RentalEquipment::getFinalCost)
//                .reduce(Money.zero(), Money::add);

        var totalFinalCost = rental.getFinalCost();
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
