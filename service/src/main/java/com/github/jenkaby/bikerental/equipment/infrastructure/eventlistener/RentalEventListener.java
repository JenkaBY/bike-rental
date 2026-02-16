package com.github.jenkaby.bikerental.equipment.infrastructure.eventlistener;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentCommandToDomainMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.UpdateEquipmentUseCase;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentRepository;
import com.github.jenkaby.bikerental.shared.domain.event.RentalStarted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RentalEventListener {

    private static final String RENTED_STATUS = "RENTED";

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
        log.info("Received RentalStarted event for equipment {}", event.equipmentId());

        try {
            Equipment equipment = equipmentRepository.findById(event.equipmentId())
                    .orElseGet(() -> {
                        log.warn("Equipment {} not found, skipping status update", event.equipmentId());
                        return null;
                    });

            if (equipment == null) {
                return;
            }

            if (RENTED_STATUS.equals(equipment.getStatusSlug())) {
                log.debug("Equipment {} already in RENTED status, skipping", event.equipmentId());
                return;
            }

            var command = equipmentCommandToDomainMapper.toUpdateCommand(equipment, RENTED_STATUS);
            updateEquipmentUseCase.execute(command);
            log.info("Successfully changed equipment {} status to RENTED", event.equipmentId());

        } catch (Exception e) {
            log.error("Failed to update equipment {} status to RENTED: {}",
                    event.equipmentId(), e.getMessage(), e);
        }
    }
}
