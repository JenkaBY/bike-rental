package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.customer.CustomerFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.service.validator.RequestedEquipmentValidator;
import com.github.jenkaby.bikerental.rental.application.usecase.CreateOrUpdateDraftRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCreated;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class CreateOrUpdateDraftRentalService implements CreateOrUpdateDraftRentalUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository repository;
    private final CustomerFacade customerFacade;
    private final EquipmentFacade equipmentFacade;
    private final EventPublisher eventPublisher;
    private final RentalEventMapper eventMapper;
    private final RequestedEquipmentValidator validator;
    private final RentalCostPolicy rentalCostPolicy;

    @Override
    @Transactional
    public Rental execute(UpdateDraftRentalCommand command) {
        Rental rental = repository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId()));

        customerFacade.findById(command.customerId())
                .orElseThrow(() -> new ReferenceNotFoundException("Customer", command.customerId().toString()));

        var equipments = equipmentFacade.findByIds(command.equipmentIds());
        validator.validateSize(command.equipmentIds(), equipments);
        validator.validateEquipmentsCondition(equipments);

        var alreadyInRental = rental.getEquipmentIds();
        var beingAdded = equipments.stream()
                .filter(e -> !alreadyInRental.contains(e.id()))
                .toList();
        validator.validateAvailability(beingAdded);

        rental.selectCustomer(command.customerId());
        rental.setPlannedDuration(command.duration());
        rental.setSpecialPriceOrDiscount(command.specialTariffId(), command.specialPrice(), command.discountPercent());
        rental.updateEquipments(equipments);

        rentalCostPolicy.recalculateEstimatedCost(rental);
        return repository.save(rental);
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
