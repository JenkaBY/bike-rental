package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.customer.CustomerFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.event.RentalCreated;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.EquipmentNotAvailableException;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.tariff.TariffFacade;
import com.github.jenkaby.bikerental.tariff.TariffInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
class CreateRentalService implements CreateRentalUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository repository;
    private final CustomerFacade customerFacade;
    private final EquipmentFacade equipmentFacade;
    private final TariffFacade tariffFacade;
    private final EventPublisher eventPublisher;
    private final RentalEventMapper eventMapper;

    CreateRentalService(
            RentalRepository repository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            TariffFacade tariffFacade,
            EventPublisher eventPublisher,
            RentalEventMapper eventMapper) {
        this.repository = repository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.tariffFacade = tariffFacade;
        this.eventPublisher = eventPublisher;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional
    public Rental execute(CreateRentalCommand command) {
        // 1. Validate customer
        customerFacade.findById(command.customerId())
                .orElseThrow(() -> new ReferenceNotFoundException(
                        "Customer",
                        command.customerId().toString()
                ));

        // 2. Validate equipment and availability
        EquipmentInfo equipment = equipmentFacade.findById(command.equipmentId())
                .orElseThrow(() -> new ReferenceNotFoundException(
                        "Equipment",
                        command.equipmentId().toString()
                ));

        if (!equipment.isAvailable()) {
            throw new EquipmentNotAvailableException(command.equipmentId(), equipment.statusSlug());
        }

        // 3. Create rental
        Rental rental = Rental.builder()
                .status(RentalStatus.DRAFT)
                .customerId(command.customerId())
                .equipmentId(command.equipmentId())
                .createdAt(Instant.now())
                .build();

        // 4. Set planned duration
        rental.setPlannedDuration(command.duration());

        // 5. Select tariff (automatically or from command)
        Long tariffId = command.tariffId() != null
                ? command.tariffId()
                : autoSelectTariff(equipment, command.duration());

        rental.selectTariff(tariffId);

        // 6. Calculate estimated cost
        TariffInfo tariff = tariffFacade.findById(tariffId)
                .orElseThrow(() -> new ReferenceNotFoundException("Tariff", tariffId.toString()));

        Money estimatedCost = tariffFacade.calculateEstimatedCost(
                tariffId,
                command.duration()
        );
        rental.setEstimatedCost(estimatedCost);

        // 7. Save
        Rental saved = repository.save(rental);

        // 8. Publish event (inter-module)
        RentalCreated event = eventMapper.toRentalCreated(saved);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);

        return saved;
    }

    @Override
    @Transactional
    public Rental execute(CreateDraftCommand command) {
        Rental rental = Rental.createDraft();
        Rental saved = repository.save(rental);

        // Publish event (inter-module)
        RentalCreated event = eventMapper.toRentalCreated(saved);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);

        return saved;
    }

    private Long autoSelectTariff(EquipmentInfo equipment, java.time.Duration duration) {
        TariffInfo selectedTariff = tariffFacade.selectTariff(
                equipment.typeSlug(),
                duration,
                java.time.LocalDate.now()
        );
        return selectedTariff.id();
    }
}
