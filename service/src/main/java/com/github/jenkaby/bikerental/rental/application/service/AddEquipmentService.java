package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.rental.application.service.validator.RequestedEquipmentValidator;
import com.github.jenkaby.bikerental.rental.application.usecase.AddEquipmentUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.port.clock.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
class AddEquipmentService implements AddEquipmentUseCase {

    private final RentalRepository rentalRepository;
    private final EquipmentFacade equipmentFacade;
    private final RequestedEquipmentValidator validator;
    private final RentalEquipmentFactory rentalEquipmentFactory;
    private final TimeProvider timeProvider;

    @Override
    @Transactional
    public @NonNull Rental execute(@NonNull AddEquipmentCommand command) {
        log.info("Adding equipment to rental {}: equipmentIds={}, operatorId={}",
                command.rentalId(), command.equipmentIds(), command.operatorId());

        Rental rental = rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));

        LocalDateTime addedAt = timeProvider.nowTruncated();
        rental.ensureCanAddEquipmentAt(addedAt);

        var equipments = equipmentFacade.findByIds(command.equipmentIds());
        validator.validateSize(command.equipmentIds(), equipments);
        validator.validateEquipmentsCondition(equipments);
        validator.validateAvailability(equipments);

        var newItems = rentalEquipmentFactory.buildActiveWithCost(rental, equipments, addedAt);
        rental.addActiveEquipments(newItems, addedAt);

        Rental saved = rentalRepository.save(rental);
        log.info("Added {} equipment(s) to rental {}", newItems.size(), saved.getId());
        return saved;
    }
}
