package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.customer.CustomerFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.UpdateRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.util.PatchValueParser;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.event.RentalStarted;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.EquipmentNotAvailableException;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.tariff.TariffFacade;
import com.github.jenkaby.bikerental.tariff.TariffInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
class UpdateRentalService implements UpdateRentalUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final CustomerFacade customerFacade;
    private final EquipmentFacade equipmentFacade;
    private final TariffFacade tariffFacade;
    private final EventPublisher eventPublisher;
    private final Clock clock;
    private final RentalEventMapper eventMapper;
    private final PatchValueParser valueParser;

    UpdateRentalService(
            RentalRepository rentalRepository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            TariffFacade tariffFacade,
            EventPublisher eventPublisher,
            Clock clock,
            RentalEventMapper eventMapper,
            PatchValueParser valueParser) {
        this.rentalRepository = rentalRepository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.tariffFacade = tariffFacade;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.eventMapper = eventMapper;
        this.valueParser = valueParser;
    }

    @Override
    @Transactional
    public Rental execute(Long rentalId, Map<String, Object> patch) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, rentalId.toString()));

        // Handle status update (rental activation)
        if (patch.containsKey("status")) {
            String newStatusStr = valueParser.parseString(patch.get("status"));
            RentalStatus newStatus = RentalStatus.valueOf(newStatusStr);

            if (RentalStatus.ACTIVE == newStatus) {
                startRental(rental);
            } else {
                // Other status changes (if needed in future)
                rental.setStatus(newStatus);
            }
        }

        // Handle customerId update
        if (patch.containsKey("customerId")) {
            UUID customerId = valueParser.parseUUID(patch.get("customerId"));
            customerFacade.findById(customerId)
                    .orElseThrow(() -> new ReferenceNotFoundException(
                            com.github.jenkaby.bikerental.customer.domain.model.Customer.class,
                            customerId.toString()
                    ));
            rental.selectCustomer(customerId);
        }

        // Handle equipmentId update
        if (patch.containsKey("equipmentId")) {
            Long equipmentId = valueParser.parseLong(patch.get("equipmentId"));
            EquipmentInfo equipment = equipmentFacade.findById(equipmentId)
                    .orElseThrow(() -> new ReferenceNotFoundException(
                            com.github.jenkaby.bikerental.equipment.domain.model.Equipment.class,
                            equipmentId.toString()
                    ));

            if (!equipment.isAvailable()) {
                throw new EquipmentNotAvailableException(equipmentId, equipment.statusSlug());
            }

            rental.selectEquipment(equipmentId);

            // Auto-select tariff if duration is already set
            if (rental.getPlannedDuration() != null && rental.getStartedAt() != null) {
                autoSelectTariffAndCalculateCost(rental, equipment);
            }
        }

        // Handle duration and startTime update (must be provided together)
        if (patch.containsKey("duration") || patch.containsKey("startTime")) {
            Duration duration = valueParser.parseDuration(patch.get("duration"));
            LocalDateTime startTime = valueParser.parseLocalDateTime(patch.get("startTime"));

            if (duration == null || startTime == null) {
                throw new IllegalArgumentException("Both duration and startTime must be provided together");
            }

            rental.setPlannedDuration(duration, startTime);

            // Auto-select tariff if equipment is already selected
            if (rental.getEquipmentId() != null) {
                EquipmentInfo equipment = equipmentFacade.findById(rental.getEquipmentId())
                        .orElseThrow();
                autoSelectTariffAndCalculateCost(rental, equipment);
            }
        }

        // Handle tariffId update (manual override)
        if (patch.containsKey("tariffId")) {
            Long tariffId = valueParser.parseLong(patch.get("tariffId"));
            tariffFacade.findById(tariffId)
                    .orElseThrow(() -> new ReferenceNotFoundException(
                            com.github.jenkaby.bikerental.tariff.domain.model.Tariff.class,
                            tariffId.toString()
                    ));
            rental.selectTariff(tariffId);

            // Recalculate cost
            if (rental.getPlannedDuration() != null && rental.getStartedAt() != null) {
                calculateCost(rental);
            }
        }

        Rental saved = rentalRepository.save(rental);
        return saved;
    }

    private void startRental(Rental rental) {
        // TODO: Validate prepayment (when US-RN-004 is implemented)
        // if (!financeFacade.hasPrepayment(rental.getId())) {
        //     throw new PrepaymentRequiredException("Prepayment must be received before starting rental");
        // }

        // Activate rental (validations are performed in Rental.activate())
        LocalDateTime actualStartTime = LocalDateTime.now(clock);
        rental.activate(actualStartTime);

        // Publish event (inter-module)
        RentalStarted event = eventMapper.toRentalStarted(rental);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
    }

    private void autoSelectTariffAndCalculateCost(Rental rental, EquipmentInfo equipment) {
        TariffInfo selectedTariff = tariffFacade.selectTariff(
                equipment.typeSlug(),
                rental.getPlannedDuration(),
                rental.getStartedAt().toLocalDate()
        );

        rental.selectTariff(selectedTariff.id());
        calculateCost(rental);
    }

    private void calculateCost(Rental rental) {
        Money cost = tariffFacade.calculateEstimatedCost(
                rental.getTariffId(),
                rental.getPlannedDuration(),
                rental.getStartedAt()
        );
        rental.setEstimatedCost(cost);
    }

}
