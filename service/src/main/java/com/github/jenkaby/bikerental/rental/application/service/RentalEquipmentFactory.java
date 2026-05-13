package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalCostCommandMapper;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.tariff.TariffV2Facade;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class RentalEquipmentFactory {

    private final TariffV2Facade tariffV2Facade;
    private final RentalCostCommandMapper costCommandMapper;

//    public List<RentalEquipment> buildAssignedAndExistingWithCost(@NonNull Rental rental, @NonNull List<EquipmentInfo> newEquipments, Map<Long, RentalEquipment> existingById) {
//
//    }


    public List<RentalEquipment> buildAssignedWithCost(@NonNull Rental rental, @NonNull List<EquipmentInfo> newEquipments) {
        if (newEquipments.isEmpty()) {
            return List.of();
        }

        var costCommand = costCommandMapper.toCommand(rental, newEquipments);
        var breakdowns = tariffV2Facade.calculateRentalCost(costCommand).equipmentBreakdowns();

        var result = new ArrayList<RentalEquipment>();
        for (int i = 0; i < newEquipments.size(); i++) {
            var equipment = newEquipments.get(i);
            var re = RentalEquipment.assigned(equipment.id(), equipment.uid(), equipment.typeSlug());
            var details = breakdowns.get(i);
            re.setEstimatedCost(details.itemCost());
            re.setTariffId(details.tariffId());
            result.add(re);
        }
        return result;
    }
}
