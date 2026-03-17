package com.github.jenkaby.bikerental.equipment.infrastructure.eventlistener;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCreated;
import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
import com.github.jenkaby.bikerental.shared.domain.event.RentalUpdated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class RentalEventListener {

    private final EquipmentRepository equipmentRepository;
    private final UpdateEquipmentUseCase updateEquipmentUseCase;
    private final EquipmentCommandToDomainMapper equipmentCommandToDomainMapper;

    public RentalEventListener(
            EquipmentRepository equipmentRepository,
            UpdateEquipmentUseCase updateEquipmentUseCase,
            EquipmentCommandToDomainMapper equipmentCommandToDomainMapper) {
        this.equipmentRepository = equipmentRepository;
        this.updateEquipmentUseCase = updateEquipmentUseCase;
        this.equipmentCommandToDomainMapper = equipmentCommandToDomainMapper;
    }

    @ApplicationModuleListener
    public void onRentalStarted(RentalStarted event) {
        log.info("Received RentalStarted event for equipments {}", event.equipmentIds());
        equipmentRepository.findByIds(event.equipmentIds())
                .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.RENTED.name()));
    }

    @ApplicationModuleListener
    public void onRentalStarted(RentalCreated event) {
        log.info("Received RentalCreated event {}", event);
        equipmentRepository.findByIds(event.equipmentIds())
                .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.RESERVED.name()));
    }

    @ApplicationModuleListener
    public void onRentalCompleted(RentalCompleted event) {
        log.info("Received RentalCompleted event for equipments {}", event.equipmentIds());
        equipmentRepository.findByIds(event.returnedEquipmentIds())
                .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.AVAILABLE.name()));
    }

    @ApplicationModuleListener
    public void onRentalUpdated(RentalUpdated event) {
        log.info("Received RentalUpdated event {}", event);
        if (CollectionUtils.isEmpty(event.currentState().equipmentIds()) && CollectionUtils.isEmpty(event.previousState().equipmentIds())) {
            return;
        }
        if (RentalStatus.isCancelled(event.currentState().rentalStatus())) {
            var ids = Stream.of(event.currentState().equipmentIds(), event.previousState().equipmentIds())
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            equipmentRepository.findByIds(ids)
                    .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.AVAILABLE.name()));
        }
        // TODO Verify this
        if (RentalStatus.isDraft(event.currentState().rentalStatus())) {
            if (!CollectionUtils.isEmpty(event.previousState().equipmentIds())) {
                equipmentRepository.findByIds(event.previousState().equipmentIds())
                        .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.AVAILABLE.name()));
            }
            if (!CollectionUtils.isEmpty(event.currentState().equipmentIds())) {
                equipmentRepository.findByIds(event.currentState().equipmentIds())
                        .forEach(equipment -> setStatusForEquipment(equipment, EquipmentStatus.RESERVED.name()));
            }
        }
    }

    private void setStatusForEquipment(Equipment equipment, String targetStatus) {
        try {
            if (targetStatus.equals(equipment.getStatusSlug())) {
                log.debug("Equipment {} already in {} status, skipping", equipment.getId(), targetStatus);
                return;
            }
            var command = equipmentCommandToDomainMapper.toUpdateCommand(equipment, targetStatus);
            updateEquipmentUseCase.execute(command);
            log.info("Successfully changed equipment {} status to {}", equipment.getId(), targetStatus);

        } catch (Exception e) {
            log.error("Failed to update equipment {} status to {}: {}", equipment.getId(), targetStatus, e.getMessage(), e);
        }
    }

    enum RentalStatus {
        DRAFT,
        ACTIVE,
        CANCELLED;

        public static boolean isCancelled(String status) {
            return CANCELLED.name().equals(status);
        }

        public static boolean isDraft(String status) {
            return DRAFT.name().equals(status);
        }
    }

    enum EquipmentStatus {
        AVAILABLE,
        RESERVED,
        RENTED
    }
}
