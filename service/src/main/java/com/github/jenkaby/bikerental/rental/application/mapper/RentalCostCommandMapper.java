package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;
import com.github.jenkaby.bikerental.rental.application.usecase.CreateRentalUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.tariff.EquipmentCostItem;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationCommand;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Mapper(uses = {EquipmentCostItemMapper.class})
public abstract class RentalCostCommandMapper {

    protected Clock clock;
    protected EquipmentCostItemMapper equipmentCostItemMapper;

    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Autowired
    public void setEquipmentCostItemMapper(EquipmentCostItemMapper equipmentCostItemMapper) {
        this.equipmentCostItemMapper = equipmentCostItemMapper;
    }

    // TODO fix these 3 methods. Seems we can replace only by one
    public RentalCostCalculationCommand toCommand(
            CreateRentalUseCase.CreateRentalCommand command,
            List<EquipmentInfo> equipments) {
        var costItems = equipmentCostItemMapper.toEquipmentCostItems(equipments);
        if (command.specialTariffId() != null) {
            return new RentalCostCalculationCommand(
                    costItems,
                    command.duration(),
                    null,
                    null,
                    command.specialTariffId(),
                    command.specialPrice(),
                    LocalDate.now(clock));
        }
        return new RentalCostCalculationCommand(
                costItems,
                command.duration(),
                null,
                command.discountPercent(),
                null,
                null,
                LocalDate.now(clock));
    }

    public RentalCostCalculationCommand toCommand(
            Rental rental,
            List<EquipmentInfo> equipments) {
        var costItems = equipmentCostItemMapper.toEquipmentCostItems(equipments);
        if (rental.getSpecialTariffId() != null) {
            return new RentalCostCalculationCommand(
                    costItems,
                    rental.getPlannedDuration(),
                    null,
                    null,
                    rental.getSpecialTariffId(),
                    rental.getSpecialPrice(),
                    LocalDate.now(clock));
        }
        return new RentalCostCalculationCommand(
                costItems,
                rental.getPlannedDuration(),
                null,
                rental.getDiscountPercent(),
                null,
                null,
                LocalDate.now(clock));
    }

    public RentalCostCalculationCommand toReturnCommand(
            Rental rental,
            List<RentalEquipment> equipmentsToReturn,
            Duration actualDuration) {
        var costItems = equipmentsToReturn.stream()
                .map(e -> new EquipmentCostItem(e.getEquipmentType()))
                .toList();
        return new RentalCostCalculationCommand(
                costItems,
                rental.getPlannedDuration(),
                actualDuration,
                rental.getDiscountPercent(),
                rental.getSpecialTariffId(),
                rental.getSpecialPrice(),
                rental.getStartedAt().toLocalDate());
    }
}
