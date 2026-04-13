package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.customer.CustomerFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalCostCommandMapper;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.service.validator.RequestedEquipmentValidator;
import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCreated;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.tariff.TariffV2Facade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;

@Slf4j
@Service
class CreateRentalService implements CreateRentalUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository repository;
    private final CustomerFacade customerFacade;
    private final EquipmentFacade equipmentFacade;
    private final TariffV2Facade tariffV2Facade;
    private final EventPublisher eventPublisher;
    private final RentalEventMapper eventMapper;
    private final RentalCostCommandMapper costCommandMapper;
    private final Clock clock;
    private final RequestedEquipmentValidator validator;
    private final FinanceFacade financeFacade;

    CreateRentalService(
            RentalRepository repository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            TariffV2Facade tariffV2Facade,
            EventPublisher eventPublisher,
            RentalEventMapper eventMapper,
            RentalCostCommandMapper costCommandMapper,
            Clock clock,
            RequestedEquipmentValidator validator,
            FinanceFacade financeFacade) {
        this.repository = repository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.tariffV2Facade = tariffV2Facade;
        this.eventPublisher = eventPublisher;
        this.eventMapper = eventMapper;
        this.costCommandMapper = costCommandMapper;
        this.clock = clock;
        this.validator = validator;
        this.financeFacade = financeFacade;
    }

    @Override
    @Transactional
    public Rental execute(CreateRentalCommand command) {
        customerFacade.findById(command.customerId())
                .orElseThrow(() -> new ReferenceNotFoundException("Customer", command.customerId().toString()));

        if (CollectionUtils.isEmpty(command.equipmentIds())) {
            throw new IllegalArgumentException("At least one equipmentId must be provided");
        }

        var equipments = equipmentFacade.findByIds(command.equipmentIds());
        validator.validateSize(command.equipmentIds(), equipments);
        validator.validateAvailability(equipments);

        var costCommand = costCommandMapper.toCommand(command, equipments);
        var costResult = tariffV2Facade.calculateRentalCost(costCommand);
        var breakdowns = costResult.equipmentBreakdowns();

        Rental rental = Rental.builder()
                .status(RentalStatus.DRAFT)
                .customerId(command.customerId())
                .createdAt(Instant.now(clock))
                .equipments(new ArrayList<>())
                .plannedDuration(command.duration())
                .specialTariffId(command.specialTariffId())
                .specialPrice(command.specialPrice())
                .discountPercent(command.discountPercent())
                .build();

        for (int i = 0; i < equipments.size(); i++) {
            var equipment = equipments.get(i);
            var rentalEquipment = RentalEquipment.assigned(
                    equipment.id(),
                    equipment.uid(),
                    equipment.typeSlug());
            rentalEquipment.setEstimatedCost(breakdowns.get(i).itemCost());
            rental.addEquipment(rentalEquipment);
        }

        Rental saved = repository.save(rental);

        if (saved.getEstimatedCost().isPositive()) {
            var holdInfo = financeFacade.holdFunds(
                    new CustomerRef(saved.getCustomerId()),
                    saved.toRentalRef(),
                    costResult.totalCost(),
                    command.operatorId());
            log.info("Funds held for rental {}: transactionId={}, heldAt={}",
                    saved.getId(), holdInfo.transactionRef().id(), holdInfo.recordedAt());
        }

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

}
