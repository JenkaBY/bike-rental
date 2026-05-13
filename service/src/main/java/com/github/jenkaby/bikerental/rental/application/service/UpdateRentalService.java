package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.customer.CustomerFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.service.validator.RequestedEquipmentValidator;
import com.github.jenkaby.bikerental.rental.application.usecase.UpdateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalPlannedDurationException;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalUpdateException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.infrastructure.util.PatchValueParser;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
class UpdateRentalService implements UpdateRentalUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final CustomerFacade customerFacade;
    private final EquipmentFacade equipmentFacade;
    private final EventPublisher eventPublisher;
    private final Clock clock;
    private final RentalEventMapper eventMapper;
    private final PatchValueParser valueParser;
    private final RequestedEquipmentValidator validator;
    private final RentalEquipmentFactory rentalEquipmentFactory;

    UpdateRentalService(
            RentalRepository rentalRepository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            EventPublisher eventPublisher,
            Clock clock,
            RentalEventMapper eventMapper,
            PatchValueParser valueParser,
            RequestedEquipmentValidator validator,
            RentalEquipmentFactory rentalEquipmentFactory) {
        this.rentalRepository = rentalRepository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.eventMapper = eventMapper;
        this.valueParser = valueParser;
        this.validator = validator;
        this.rentalEquipmentFactory = rentalEquipmentFactory;
    }

    @Override
    @Transactional
    public Rental execute(Long rentalId, Map<String, Object> patch) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, rentalId.toString()));
        var previousState = eventMapper.toRentalState(rental);

        if (patch.containsKey("customerId")) {
            UUID customerId = valueParser.parseUUID(patch.get("customerId"));
            customerFacade.findById(customerId)
                    .orElseThrow(() -> new ReferenceNotFoundException("Customer", customerId.toString()));
            rental.selectCustomer(customerId);
        }

        if (patch.containsKey("duration")) {
            Integer duration = valueParser.parseInt(patch.get("duration"));
            if (duration == null) {
                throw new InvalidRentalUpdateException("Duration must be provided");
            }
            rental.setPlannedDuration(Duration.ofMinutes(duration));
        }

        if (patch.containsKey("equipmentIds")) {
            List<Long> equipmentIds = valueParser.parseListOfLong(patch.get("equipmentIds"));
            List<EquipmentInfo> foundEquipments = equipmentFacade.findByIds(equipmentIds);
            validator.validateSize(equipmentIds, foundEquipments);

            var alreadyReservedOrRented = new HashSet<>(previousState.equipmentIds());
            var beingReserved = foundEquipments.stream()
                    .filter(e -> !alreadyReservedOrRented.contains(e.id()))
                    .toList();
            validator.validateEquipmentsCondition(beingReserved);
            validator.validateAvailability(beingReserved);

            if (rental.getPlannedDuration() == null) {
                throw new InvalidRentalPlannedDurationException(rental.getId());
            }

            var incomingIds = foundEquipments.stream()
                    .map(EquipmentInfo::id)
                    .collect(Collectors.toSet());
            var newEquipmentIds = rental.getNewEquipmentIds(incomingIds);
            var newEquipments = foundEquipments.stream()
                    .filter(e -> newEquipmentIds.contains(e.id()))
                    .toList();

            var equipmentsToAdd = rentalEquipmentFactory.buildAssignedWithCost(rental, newEquipments);
            rental.replaceEquipments(equipmentsToAdd, incomingIds);
        }

        var currentState = eventMapper.toRentalState(rental);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, eventMapper.toRentalUpdated(rental, previousState, currentState));

        return rentalRepository.save(rental);
    }

}
