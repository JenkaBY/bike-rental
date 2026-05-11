package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.domain.repository.RentalEquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class EquipmentAvailabilityService {

    private final RentalEquipmentRepository rentalEquipmentRepository;

    public EquipmentAvailabilityService(RentalEquipmentRepository rentalEquipmentRepository) {
        this.rentalEquipmentRepository = rentalEquipmentRepository;
    }

    public Set<Long> getUnavailableIds(Set<Long> equipmentIds) {
        if (equipmentIds.isEmpty()) {
            return Set.of();
        }
        return rentalEquipmentRepository.findOccupiedEquipmentIds(equipmentIds);
    }
}