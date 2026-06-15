package com.github.jenkaby.bikerental.rental.application;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalCostCommandMapper;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEquipmentCostBreakdownMapper;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.vo.EquipmentCostResult;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationV2Command;
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
    private final RentalEquipmentCostBreakdownMapper breakdownMapper;

    public List<EquipmentCostResult> calculateEstimated(Rental rental, List<EquipmentInfo> equipments) {
        var command = costCommandMapper.toEstimateCommand(rental, equipments);
        return extractEstimatedResults(equipments, command);
    }

    public List<EquipmentCostResult> calculateFinal(Rental rental, List<EquipmentInfo> equipments, Duration billableDuration) {
        var command = costCommandMapper.toReturnCommand(rental, equipments, billableDuration);
        return extractFinalResults(equipments, command);
    }

    private List<EquipmentCostResult> extractEstimatedResults(List<EquipmentInfo> equipments, RentalCostCalculationV2Command command) {
        var breakdowns = tariffFacade.calculateRentalCostV2(command).equipmentBreakdowns();

        var results = new ArrayList<EquipmentCostResult>();
        for (int i = 0; i < equipments.size(); i++) {
            var breakdown = breakdowns.get(i);
            results.add(EquipmentCostResult.withoutBreakdown(
                    equipments.get(i).id(),
                    breakdown.tariffId(),
                    breakdown.itemCost()
            ));
        }
        return results;
    }

    private List<EquipmentCostResult> extractFinalResults(List<EquipmentInfo> equipments, RentalCostCalculationV2Command command) {
        var breakdowns = tariffFacade.calculateRentalCostV2(command).equipmentBreakdowns();

        var results = new ArrayList<EquipmentCostResult>();
        for (int i = 0; i < equipments.size(); i++) {
            var breakdown = breakdowns.get(i);
            results.add(new EquipmentCostResult(
                    equipments.get(i).id(),
                    breakdown.tariffId(),
                    breakdown.itemCost(),
                    breakdownMapper.toBreakdown(breakdown)
            ));
        }
        return results;
    }
}
