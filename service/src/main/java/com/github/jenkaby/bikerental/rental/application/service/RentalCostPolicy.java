package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.rental.application.RentalEquipmentCostCalculator;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalPlannedDurationException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalCostPolicy {

    private final RentalEquipmentCostCalculator calculator;
    private final EquipmentFacade equipmentFacade;

    public void recalculateEstimatedCost(Rental rental) {
        if (rental.getPlannedDuration() == null) {
            throw new InvalidRentalPlannedDurationException(rental.getId());
        }
        var equipments = equipmentFacade.findByIds(new ArrayList<>(rental.notReturnedEquipmentIds()));
        var results = calculator.calculateEstimated(rental, equipments);
        rental.applyEstimatedCost(results);
    }

    public void calculateFinalCost(Rental rental, List<RentalEquipment> returningEquipments,
                                   RentalDurationCalculator durationCalculator, LocalDateTime returnTime) {
        var results = calculator.calculateFinal(rental, returningEquipments, durationCalculator, returnTime);
        rental.applyFinalCost(results);
    }
}