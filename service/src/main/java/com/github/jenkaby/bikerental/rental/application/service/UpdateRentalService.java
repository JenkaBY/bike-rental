package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.customer.CustomerFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.UpdateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalUpdateException;
import com.github.jenkaby.bikerental.rental.domain.exception.PrepaymentRequiredException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.infrastructure.util.PatchValueParser;
import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.EquipmentNotAvailableException;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.tariff.TariffFacade;
import com.github.jenkaby.bikerental.tariff.TariffInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
class UpdateRentalService implements UpdateRentalUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final CustomerFacade customerFacade;
    private final EquipmentFacade equipmentFacade;
    private final TariffFacade tariffFacade;
    private final FinanceFacade financeFacade;
    private final EventPublisher eventPublisher;
    private final Clock clock;
    private final RentalEventMapper eventMapper;
    private final PatchValueParser valueParser;

    UpdateRentalService(
            RentalRepository rentalRepository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            TariffFacade tariffFacade,
            FinanceFacade financeFacade,
            EventPublisher eventPublisher,
            Clock clock,
            RentalEventMapper eventMapper,
            PatchValueParser valueParser) {
        this.rentalRepository = rentalRepository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.tariffFacade = tariffFacade;
        this.financeFacade = financeFacade;
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

        // Handle customerId update
        if (patch.containsKey("customerId")) {
            UUID customerId = valueParser.parseUUID(patch.get("customerId"));
            customerFacade.findById(customerId)
                    .orElseThrow(() -> new ReferenceNotFoundException("Customer", customerId.toString()));
            rental.selectCustomer(customerId);
        }

        // Handle equipmentId update
        EquipmentInfo equipment = null;
        if (patch.containsKey("equipmentId")) {
            Long equipmentId = valueParser.parseLong(patch.get("equipmentId"));
            equipment = equipmentFacade.findById(equipmentId)
                    .orElseThrow(() -> new ReferenceNotFoundException("Equipment", equipmentId.toString()));

            if (!equipment.isAvailable()) {
                throw new EquipmentNotAvailableException(equipmentId, equipment.statusSlug());
            }

            rental.selectEquipment(equipmentId);
            rental.setEquipmentUid(equipment.uid()); // Update equipment UID when equipment changes
        }

        // Handle duration update
        if (patch.containsKey("duration")) {
            Duration duration = valueParser.parseDuration(patch.get("duration"));
            if (duration == null) {
                throw new InvalidRentalUpdateException("Duration must be provided");
            }
            rental.setPlannedDuration(duration);
        }

        // Auto-select tariff if equipment and duration are set, and tariff is not manually set
        if (patch.containsKey("tariffId")) {
            // Handle tariffId update (manual override)
            Long tariffId = valueParser.parseLong(patch.get("tariffId"));
            tariffFacade.findById(tariffId)
                    .orElseThrow(() -> new ReferenceNotFoundException("Tariff", tariffId.toString()));
            rental.selectTariff(tariffId);
        } else if (rental.getEquipmentId() != null && rental.getPlannedDuration() != null) {
            // Auto-select tariff if equipment and duration are already set
            if (equipment == null) {
                equipment = equipmentFacade.findById(rental.getEquipmentId())
                        .orElseThrow(() -> new ReferenceNotFoundException("Equipment", rental.getEquipmentId().toString()));
            }
            autoSelectTariff(rental, equipment);
        }

        // Calculate cost if tariff and duration are set
        if (rental.getTariffId() != null && rental.getPlannedDuration() != null) {
            calculateCost(rental);
        }

        // Handle status update (rental activation)
        if (patch.containsKey("status")) {
            String newStatusStr = valueParser.parseString(patch.get("status"));
            RentalStatus newStatus = RentalStatus.valueOf(newStatusStr);
            log.info("Updating rental {} status to {}", rental, newStatus);
            if (RentalStatus.ACTIVE == newStatus) {
                startRental(rental);
            } else {
                // Other status changes (if needed in future)
                rental.setStatus(newStatus);
            }
        }

        Rental saved = rentalRepository.save(rental);
        return saved;
    }

    private void startRental(Rental rental) {
        if (!financeFacade.hasPrepayment(rental.getId())) {
            throw new PrepaymentRequiredException(rental.getId());
        }

        // Activate rental (validations are performed in Rental.activate())
        LocalDateTime actualStartTime = LocalDateTime.now(clock);
        rental.activate(actualStartTime);

        // Publish event (inter-module)
        RentalStarted event = eventMapper.toRentalStarted(rental);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
    }

    private void autoSelectTariff(Rental rental, EquipmentInfo equipment) {
        TariffInfo selectedTariff = tariffFacade.selectTariff(
                equipment.typeSlug(),
                rental.getPlannedDuration(),
                LocalDate.now(clock));

        rental.selectTariff(selectedTariff.id());
    }

    private void calculateCost(Rental rental) {
        Money cost = tariffFacade.calculateEstimatedCost(
                rental.getTariffId(),
                rental.getPlannedDuration());
        rental.setEstimatedCost(cost);
    }

}
