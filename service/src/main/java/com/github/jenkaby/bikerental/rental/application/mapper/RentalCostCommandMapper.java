package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.shared.infrastructure.port.clock.TimeProvider;
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

    protected TimeProvider timeProvider;

    @Autowired
    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public RentalCostCalculationV2Command toEstimateCommand(Rental rental, List<EquipmentInfo> equipments) {
        var startAt = timeProvider.today().atStartOfDay();
        var costItems = equipments.stream()
                .map(equipment -> new EquipmentCostItemV2(equipment.id(), equipment.typeSlug(), null, null, null))
                .toList();
        return buildCommand(rental, costItems, rental.getPlannedDuration(), startAt);
    }

    public RentalCostCalculationV2Command toMidRentalEstimateCommand(Rental rental, List<EquipmentInfo> equipments,
                                                                     Duration remainingDuration, LocalDateTime startAt) {
        var costItems = equipments.stream()
                .map(equipment -> new EquipmentCostItemV2(equipment.id(), equipment.typeSlug(), null, null, null))
                .toList();
        return buildCommand(rental, costItems, remainingDuration, startAt);
    }

    public RentalCostCalculationV2Command toReturnCommand(Rental rental, List<RentalEquipment> returningEquipments,
                                                          RentalDurationCalculator durationCalculator, LocalDateTime returnTime) {
        var costItems = returningEquipments.stream()
                .map(equipment -> toReturnCostItem(equipment, durationCalculator, returnTime))
                .toList();
        return buildCommand(rental, costItems, rental.getPlannedDuration(), rental.getStartedAt());
    }

    private EquipmentCostItemV2 toReturnCostItem(RentalEquipment equipment, RentalDurationCalculator durationCalculator,
                                                 LocalDateTime returnTime) {
        var itemStartAt = equipment.getStartedAt();
        var billableDuration = durationCalculator.calculate(itemStartAt, returnTime).billableDuration();
        var itemPlannedDuration = Duration.between(itemStartAt, equipment.getExpectedReturnAt());
        var itemReturnAt = itemStartAt.plus(billableDuration);
        return new EquipmentCostItemV2(equipment.getEquipmentId(), equipment.getEquipmentType(), itemStartAt, itemPlannedDuration, itemReturnAt);
    }

    private RentalCostCalculationV2Command buildCommand(Rental rental, List<EquipmentCostItemV2> costItems,
                                                        Duration plannedDuration, LocalDateTime startAt) {
        if (rental.getSpecialTariffId() != null) {
            return new RentalCostCalculationV2Command(
                    costItems,
                    plannedDuration,
                    null,
                    rental.getSpecialTariffId(),
                    rental.getSpecialPrice(),
                    startAt);
        }
        return new RentalCostCalculationV2Command(
                costItems,
                plannedDuration,
                rental.getDiscountPercent(),
                null,
                null,
                startAt);
    }
}
