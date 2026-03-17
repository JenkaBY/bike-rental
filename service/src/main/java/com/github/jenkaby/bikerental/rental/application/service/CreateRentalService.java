package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.customer.CustomerFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.service.validator.RequestedEquipmentValidator;
import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCreated;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.tariff.TariffFacade;
import com.github.jenkaby.bikerental.tariff.TariffInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;

@Service
class CreateRentalService implements CreateRentalUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository repository;
    private final CustomerFacade customerFacade;
    private final EquipmentFacade equipmentFacade;
    private final TariffFacade tariffFacade;
    private final EventPublisher eventPublisher;
    private final RentalEventMapper eventMapper;
    private final Clock clock;
    private final RequestedEquipmentValidator validator;

    CreateRentalService(
            RentalRepository repository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            TariffFacade tariffFacade,
            EventPublisher eventPublisher,
            RentalEventMapper eventMapper, Clock clock, RequestedEquipmentValidator validator) {
        this.repository = repository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.tariffFacade = tariffFacade;
        this.eventPublisher = eventPublisher;
        this.eventMapper = eventMapper;
        this.clock = clock;
        this.validator = validator;
    }

    @Override
    @Transactional
    public Rental execute(CreateRentalCommand command) {
        customerFacade.findById(command.customerId())
                .orElseThrow(() -> new ReferenceNotFoundException("Customer", command.customerId().toString()));

        if (CollectionUtils.isEmpty(command.equipmentIds())) {
            throw new IllegalArgumentException("At least one equipmentId must be provided");
        }
        Rental rental = Rental.builder()
                .status(RentalStatus.DRAFT)
                .customerId(command.customerId())
                .createdAt(Instant.now())
                .equipments(new ArrayList<>())
                .plannedDuration(command.duration())
                .build();

        var equipments = equipmentFacade.findByIds(command.equipmentIds());
        validator.validateSize(command.equipmentIds(), equipments);
        validator.validateAvailability(equipments);

        for (var equipment : equipments) {
            var selectedTariffId = autoSelectTariff(equipment, command.duration());
            var cost = tariffFacade.calculateRentalCost(selectedTariffId, command.duration());
            var rentalEquipment = RentalEquipment.assigned(equipment.id(), equipment.uid());
            rentalEquipment.setEstimatedCost(cost.totalCost());
            rentalEquipment.setTariffId(selectedTariffId);
            rental.addEquipment(rentalEquipment);
        }

        Rental saved = repository.save(rental);

        RentalCreated event = eventMapper.toRentalCreated(saved);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
        return saved;
    }

    @Override
    @Transactional
    public Rental execute(CreateDraftCommand command) {
        var draft = repository.save(Rental.createDraft());
        RentalCreated event = eventMapper.toRentalCreated(draft);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
        return draft;
    }

    private Long autoSelectTariff(EquipmentInfo equipment, Duration duration) {
        TariffInfo selectedTariff = tariffFacade.selectTariff(
                equipment.typeSlug(),
                duration,
                LocalDate.now(clock)
        );
        return selectedTariff.id();
    }
}
