package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.equipment.EquipmentFacade;
import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.application.RentalEquipmentCostCalculator;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalPlannedDurationException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
        var equipments = equipmentFacade.findByIds(new ArrayList<>(rental.getEquipmentIds()));
        var results = calculator.calculateEstimated(rental, equipments);
        rental.applyEstimatedCost(results);
    }

    public void calculateFinalCost(Rental rental, List<EquipmentInfo> returningEquipments, Duration billableDuration) {
        var results = calculator.calculateFinal(rental, returningEquipments, billableDuration);
        rental.applyFinalCost(results);
    }
}