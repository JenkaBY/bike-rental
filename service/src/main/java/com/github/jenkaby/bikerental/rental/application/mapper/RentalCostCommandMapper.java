package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.tariff.EquipmentCostItemV2;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationV2Command;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public abstract class RentalCostCommandMapper {

    protected Clock clock;

    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public RentalCostCalculationV2Command toEstimateCommand(Rental rental, List<EquipmentInfo> equipments) {
        var startAt = LocalDate.now(clock).atStartOfDay();
        var costItems = equipments.stream()
                .map(equipment -> new EquipmentCostItemV2(equipment.id(), equipment.typeSlug(), null))
                .toList();
        return buildCommand(rental, costItems, startAt);
    }

    public RentalCostCalculationV2Command toReturnCommand(Rental rental, List<EquipmentInfo> equipments, Duration billableDuration) {
        var startAt = rental.getStartedAt();
        var returnAt = startAt.plus(billableDuration);
        var costItems = equipments.stream()
                .map(equipment -> new EquipmentCostItemV2(equipment.id(), equipment.typeSlug(), returnAt))
                .toList();
        return buildCommand(rental, costItems, startAt);
    }

    private RentalCostCalculationV2Command buildCommand(Rental rental, List<EquipmentCostItemV2> costItems, LocalDateTime startAt) {
        if (rental.getSpecialTariffId() != null) {
            return new RentalCostCalculationV2Command(
                    costItems,
                    rental.getPlannedDuration(),
                    null,
                    rental.getSpecialTariffId(),
                    rental.getSpecialPrice(),
                    startAt);
        }
        return new RentalCostCalculationV2Command(
                costItems,
                rental.getPlannedDuration(),
                rental.getDiscountPercent(),
                null,
                null,
                startAt);
    }
}
