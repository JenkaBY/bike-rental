package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.customer.CustomerFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalCostCommandMapper;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.application.service.validator.RequestedEquipmentValidator;
import com.github.jenkaby.bikerental.rental.application.usecase.UpdateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.HoldRequiredException;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalPlannedDurationException;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalUpdateException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.infrastructure.util.PatchValueParser;
import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.tariff.TariffV2Facade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
class UpdateRentalService implements UpdateRentalUseCase {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final CustomerFacade customerFacade;
    private final EquipmentFacade equipmentFacade;
    private final TariffV2Facade tariffV2Facade;
    private final FinanceFacade financeFacade;
    private final EventPublisher eventPublisher;
    private final Clock clock;
    private final RentalEventMapper eventMapper;
    private final RentalCostCommandMapper costCommandMapper;
    private final PatchValueParser valueParser;
    private final RequestedEquipmentValidator validator;

    UpdateRentalService(
            RentalRepository rentalRepository,
            CustomerFacade customerFacade,
            EquipmentFacade equipmentFacade,
            TariffV2Facade tariffV2Facade,
            FinanceFacade financeFacade,
            EventPublisher eventPublisher,
            Clock clock,
            RentalEventMapper eventMapper,
            RentalCostCommandMapper costCommandMapper,
            PatchValueParser valueParser,
            RequestedEquipmentValidator validator) {
        this.rentalRepository = rentalRepository;
        this.customerFacade = customerFacade;
        this.equipmentFacade = equipmentFacade;
        this.tariffV2Facade = tariffV2Facade;
        this.financeFacade = financeFacade;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.eventMapper = eventMapper;
        this.costCommandMapper = costCommandMapper;
        this.valueParser = valueParser;
        this.validator = validator;
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
            Duration duration = valueParser.parseDuration(patch.get("duration"));
            if (duration == null) {
                throw new InvalidRentalUpdateException("Duration must be provided");
            }
            rental.setPlannedDuration(duration);
        }

        List<EquipmentInfo> equipments = new ArrayList<>();
        if (patch.containsKey("equipmentIds")) {
            List<Long> equipmentIds = valueParser.parseListOfLong(patch.get("equipmentIds"));
            List<EquipmentInfo> foundEquipments = equipmentFacade.findByIds(equipmentIds);
            validator.validateSize(equipmentIds, foundEquipments);
            var alreadyReservedOrRented = previousState.equipmentIds();
            var beingReserved = foundEquipments.stream()
                    .filter(e -> !alreadyReservedOrRented.contains(e.id()))
                    .toList();
            validator.validateAvailability(beingReserved);
            equipments.addAll(foundEquipments);

            if (rental.getPlannedDuration() == null) {
                throw new InvalidRentalPlannedDurationException(rental.getId());
            }

            var costCommand = costCommandMapper.toCommand(rental, foundEquipments);
            var costResult = tariffV2Facade.calculateRentalCost(costCommand);
            var breakdowns = costResult.equipmentBreakdowns();

            rental.clearEquipmentRentals();
            for (int i = 0; i < foundEquipments.size(); i++) {
                var equipment = foundEquipments.get(i);
                RentalEquipment rentalEquipment = RentalEquipment.assigned(
                        equipment.id(),
                        equipment.uid(),
                        equipment.typeSlug());
                rentalEquipment.setEstimatedCost(breakdowns.get(i).itemCost());
                rental.addEquipment(rentalEquipment);
            }
        }


        if (patch.containsKey("status")) {
            String newStatusStr = valueParser.parseString(patch.get("status"));
            RentalStatus newStatus = RentalStatus.valueOf(newStatusStr);
            log.info("Updating rental {} status to {}", rental, newStatus);

            if (RentalStatus.ACTIVE == newStatus) {
                startRental(rental);
            } else {
                rental.setStatus(newStatus);
            }
        }
        if (rental.getStatus() != RentalStatus.ACTIVE) {
            var currentState = eventMapper.toRentalState(rental);
            eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, eventMapper.toRentalUpdated(rental, previousState, currentState));
        }

        return rentalRepository.save(rental);
    }

    private void startRental(Rental rental) {
        if (rental.getEstimatedCost().isPositive() && !financeFacade.hasHold(rental.toRentalRef())) {
            throw new HoldRequiredException(rental.getId());
        }

        // Activate rental (validations are performed in Rental.activate())
        LocalDateTime actualStartTime = LocalDateTime.now(clock);
        rental.activate(actualStartTime);

        // Publish event (inter-module)
        RentalStarted event = eventMapper.toRentalStarted(rental);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, event);
    }


}
