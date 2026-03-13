package com.github.jenkaby.bikerental.equipment.infrastructure.eventlistener;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.shared.domain.event.RentalCompleted;
import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RentalEventListener {

    private static final String RENTED_STATUS = "RENTED";
    private static final String AVAILABLE_STATUS = "AVAILABLE";

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
                .forEach(equipment -> setStatusForEquipment(equipment, RENTED_STATUS));
    }

    @ApplicationModuleListener
    public void onRentalCompleted(RentalCompleted event) {
        log.info("Received RentalCompleted event for equipments {}", event.equipmentIds());
        equipmentRepository.findByIds(event.equipmentIds())
                .forEach(equipment -> setStatusForEquipment(equipment, AVAILABLE_STATUS));
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
}
