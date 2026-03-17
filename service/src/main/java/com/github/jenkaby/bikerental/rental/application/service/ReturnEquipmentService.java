package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentResult;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipmentStatus;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.tariff.RentalCost;
import com.github.jenkaby.bikerental.tariff.TariffFacade;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
class ReturnEquipmentService implements ReturnEquipmentUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final RentalDurationCalculator durationCalculator;
    private final TariffFacade tariffFacade;
    private final FinanceFacade financeFacade;
    private final RentalEventMapper eventMapper;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    ReturnEquipmentService(
            RentalRepository rentalRepository,
            RentalDurationCalculator durationCalculator,
            TariffFacade tariffFacade,
            FinanceFacade financeFacade,
            RentalEventMapper eventMapper,
            EventPublisher eventPublisher,
            Clock clock) {
        this.rentalRepository = rentalRepository;
        this.durationCalculator = durationCalculator;
        this.tariffFacade = tariffFacade;
        this.financeFacade = financeFacade;
        this.eventMapper = eventMapper;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    @NonNull
    public ReturnEquipmentResult execute(@NonNull ReturnEquipmentCommand command) {
        log.info("Processing equipment return for rentalId={}, equipmentIds={}, equipmentUids={}",
                command.rentalId(), command.equipmentIds(), command.equipmentUids());

        LocalDateTime returnTime = LocalDateTime.now(clock);
        Rental rental = findRental(command);
        if (!rental.hasActiveStatus()) {
            throw new InvalidRentalStatusException(rental.getStatus(), RentalStatus.ACTIVE);
        }
        var durationResult = rental.calculateActualDuration(durationCalculator, returnTime);
        var equipmentsToReturn = rental.equipmentsToReturn(command.getEquipmentIds(), command.getEquipmentUids(), returnTime);
        Money totalCost = Money.zero();
        Map<Long, RentalCost> rentalCostMapToEquipment = new HashMap<>();
        for (var equipment : equipmentsToReturn) {
            RentalCost cost = tariffFacade.calculateRentalCost(
                    equipment.getTariffId(),
                    rental.getActualDuration(),
                    durationResult.billableMinutes(),
                    rental.getPlannedDuration()
            );
            equipment.setFinalCost(cost.totalCost());
            rentalCostMapToEquipment.put(equipment.getEquipmentId(), cost);
            totalCost = totalCost.add(cost.totalCost());
        }
// 1. Sum final costs of equipment returned in PREVIOUS partial returns
        Money previouslyReturnedCost = rental.getEquipments().stream()
                .filter(e -> e.getStatus() == RentalEquipmentStatus.RETURNED)
                .filter(e -> !equipmentsToReturn.contains(e))
                .map(RentalEquipment::getFinalCost)
                .reduce(Money.zero(), Money::add);

// 2. Estimated cost of equipment STILL ACTIVE after this return
        Money remainingEstimatedCost = rental.getEquipments().stream()
                .filter(e -> e.getStatus() != RentalEquipmentStatus.RETURNED)
                .map(RentalEquipment::getEstimatedCost)
                .reduce(Money.zero(), Money::add);


        List<PaymentInfo> paymentsMade = financeFacade.getPayments(rental.getId());
        Money paymentsTotalAmount = paymentsMade.stream()
                .map(PaymentInfo::amount)
                .reduce(Money.zero(), Money::add);

// 3. Correct balance:
// toPay = previouslyReturned + currentReturned + remainingEstimated - allPayments
        Money toPay = previouslyReturnedCost
                .add(totalCost)
                .add(remainingEstimatedCost)
                .subtract(paymentsTotalAmount);
//        Money toPay = totalCost.subtract(paymentsTotalAmount);

        PaymentInfo paymentInfo = null;
        if (toPay.isPositive()) {
            if (command.paymentMethod() == null) {
                throw new IllegalArgumentException("Payment method is required when additional payment is needed");
            }
            paymentInfo = financeFacade.recordAdditionalPayment(
                    rental.getId(),
                    toPay,
                    command.paymentMethod(),
                    command.operatorId()
            );
            log.info("Recorded additional payment {} for rental {}", toPay, rental.getId());
        }

        rental.complete(paymentsTotalAmount.add(Optional.ofNullable(paymentInfo).map(PaymentInfo::amount).orElse(Money.zero())));

        Rental saved = rentalRepository.save(rental);

        RentalCompleted event = eventMapper.toRentalCompleted(saved, returnTime, totalCost);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
        log.info("Published RentalCompleted event for rental {}", saved.getId());

        return new ReturnEquipmentResult(saved, rentalCostMapToEquipment, toPay, paymentInfo);
    }

    private Rental findRental(ReturnEquipmentCommand command) {
        return rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));
    }
}
