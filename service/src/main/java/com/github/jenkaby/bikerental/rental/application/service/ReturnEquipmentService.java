package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.FindRentalsUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentResult;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
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

@Slf4j
@Service
class ReturnEquipmentService implements ReturnEquipmentUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final FindRentalsUseCase findRentalsUseCase;
    private final RentalDurationCalculator durationCalculator;
    private final TariffFacade tariffFacade;
    private final FinanceFacade financeFacade;
    private final EquipmentFacade equipmentFacade;
    private final RentalEventMapper eventMapper;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    ReturnEquipmentService(
            RentalRepository rentalRepository,
            FindRentalsUseCase findRentalsUseCase,
            RentalDurationCalculator durationCalculator,
            TariffFacade tariffFacade,
            FinanceFacade financeFacade,
            EquipmentFacade equipmentFacade,
            RentalEventMapper eventMapper,
            EventPublisher eventPublisher,
            Clock clock) {
        this.rentalRepository = rentalRepository;
        this.findRentalsUseCase = findRentalsUseCase;
        this.durationCalculator = durationCalculator;
        this.tariffFacade = tariffFacade;
        this.financeFacade = financeFacade;
        this.equipmentFacade = equipmentFacade;
        this.eventMapper = eventMapper;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    @NonNull
    public ReturnEquipmentResult execute(@NonNull ReturnEquipmentCommand command) {
        log.info("Processing equipment return for rentalId={}, equipmentId={}, equipmentUid={}",
                command.rentalId(), command.equipmentId(), command.equipmentUid());

        // Use current time as return time
        LocalDateTime returnTime = LocalDateTime.now(clock);

        // 1. Find rental by one of the identifiers
        Rental rental = findRental(command);

        // 2. Validate status
        if (!rental.hasActiveStatus()) {
            throw new InvalidRentalStatusException(rental.getStatus(), RentalStatus.ACTIVE);
        }

        // 3. Calculate actual duration and record return time on rental
        var durationResult = rental.calculateActualDuration(durationCalculator, returnTime);

        // 4. Calculate final cost via tariff facade (public API, respects module boundaries)
        RentalCost cost = tariffFacade.calculateFinalCost(
                rental.getTariffId(),
                rental.getActualDuration(),
                durationResult.billableMinutes(),
                rental.getPlannedDuration()
        );

        // 5. Get prepayment already collected
        Money prepaymentAmount = financeFacade.getPrepayment(rental.getId())
                .map(PaymentInfo::amount)
                .orElse(Money.zero());

        // 6. Calculate additional payment required
        Money additionalPayment = cost.totalCost().subtract(prepaymentAmount);

        // 7. Record additional payment if needed
        PaymentInfo paymentInfo = null;
        if (additionalPayment.isPositive()) {
            if (command.paymentMethod() == null) {
                throw new IllegalArgumentException("Payment method is required when additional payment is needed");
            }
            paymentInfo = financeFacade.recordAdditionalPayment(
                    rental.getId(),
                    additionalPayment,
                    command.paymentMethod(),
                    command.operatorId()
            );
            log.info("Recorded additional payment {} for rental {}", additionalPayment, rental.getId());
        }

        // 8. Complete rental (duration already set in step 3)
        rental.complete(cost.totalCost());

        // 9. Save rental
        Rental saved = rentalRepository.save(rental);

        // 10. Publish event
        RentalCompleted event = eventMapper.toRentalCompleted(saved, returnTime, cost.totalCost());
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
        log.info("Published RentalCompleted event for rental {}", saved.getId());

        return new ReturnEquipmentResult(saved, cost, additionalPayment, paymentInfo);
    }

    private Rental findRental(ReturnEquipmentCommand command) {
        // Priority: rentalId > equipmentUid > equipmentId
        if (command.rentalId() != null) {
            return rentalRepository.findById(command.rentalId())
                    .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));
        }

        String equipmentUid;
        if (command.equipmentUid() != null) {
            equipmentUid = command.equipmentUid();
        } else if (command.equipmentId() != null) {
            equipmentUid = equipmentFacade.findById(command.equipmentId())
                    .map(com.github.jenkaby.bikerental.equipment.EquipmentInfo::uid)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Equipment",
                            command.equipmentId().toString()
                    ));
        } else {
            throw new IllegalArgumentException("Either rentalId, equipmentId, or equipmentUid must be provided");
        }

        var query = new FindRentalsUseCase.FindRentalsQuery(
                RentalStatus.ACTIVE,
                null,
                equipmentUid,
                PageRequest.singleItem()
        );

        Page<Rental> rentals = findRentalsUseCase.execute(query);

        if (rentals.items().isEmpty()) {
            String identifier = command.equipmentUid() != null
                    ? "equipmentUid: " + command.equipmentUid()
                    : "equipmentId: " + command.equipmentId();
            throw new ResourceNotFoundException(Rental.class, "No active rental found for " + identifier);
        }

        return rentals.items().getFirst();
    }
}
