package com.github.jenkaby.bikerental.rental.application;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalCostCommandMapper;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.vo.EquipmentCostResult;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationCommand;
import com.github.jenkaby.bikerental.tariff.TariffV2Facade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalEquipmentCostCalculator {

    private final TariffV2Facade tariffFacade;
    private final RentalCostCommandMapper costCommandMapper;

    public List<EquipmentCostResult> calculateEstimated(Rental rental, List<EquipmentInfo> equipments) {
        var command = costCommandMapper.toCommand(rental, equipments);
        return extractResults(equipments, command);
    }

    public List<EquipmentCostResult> calculateFinal(Rental rental, List<EquipmentInfo> equipments, Duration billableDuration) {
        var command = costCommandMapper.toReturnCommand(rental, equipments, billableDuration);
        return extractResults(equipments, command);
    }

    private List<EquipmentCostResult> extractResults(List<EquipmentInfo> equipments, RentalCostCalculationCommand command) {
        var breakdowns = tariffFacade.calculateRentalCost(command).equipmentBreakdowns();

        var results = new ArrayList<EquipmentCostResult>();
        for (int i = 0; i < equipments.size(); i++) {
            var breakdown = breakdowns.get(i);
            results.add(new EquipmentCostResult(
                    equipments.get(i).id(),
                    breakdown.tariffId(),
                    breakdown.itemCost()
            ));
        }
        return results;
    }
}