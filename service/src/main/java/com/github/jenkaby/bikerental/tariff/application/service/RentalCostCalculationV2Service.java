package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.config.RentalProperties;
import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.*;
import com.github.jenkaby.bikerental.tariff.application.usecase.RentalCostCalculationV2UseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.SelectTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.domain.exception.InvalidSpecialPriceException;
import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffV2Repository;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseRentalCostCalculationResult;
import com.github.jenkaby.bikerental.tariff.domain.service.EquipmentCostBreakdownV2;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
class RentalCostCalculationV2Service implements RentalCostCalculationV2UseCase {

    private final RentalProperties rentalProperties;
    private final TariffV2Repository tariffRepository;
    private final SelectTariffV2UseCase selectTariffUseCase;

    RentalCostCalculationV2Service(RentalProperties rentalProperties,
                                   TariffV2Repository tariffRepository,
                                   SelectTariffV2UseCase selectTariffUseCase) {
        this.rentalProperties = rentalProperties;
        this.tariffRepository = tariffRepository;
        this.selectTariffUseCase = selectTariffUseCase;
    }

    @Override
    public RentalCostCalculationResult execute(RentalCostCalculationV2Command command) {
        if (command.specialTariffId() != null) {
            return executeSpecialMode(command);
        }
        return executeNormalMode(command);
    }

    private RentalCostCalculationResult executeSpecialMode(RentalCostCalculationV2Command command) {
        var specialTariff = tariffRepository.get(command.specialTariffId());
        if (specialTariff.getPricingType() != PricingType.SPECIAL) {
            throw new InvalidSpecialTariffTypeException(command.specialTariffId(), specialTariff.getPricingType());
        }
        Money totalCost = command.specialPrice();
        if (totalCost.isNegative()) {
            throw new InvalidSpecialPriceException();
        }

        List<EquipmentCostBreakdown> breakdowns = new ArrayList<>();
        Duration planned = command.plannedDuration();
        for (EquipmentCostItemV2 item : command.equipments()) {
            breakdowns.add(new EquipmentCostBreakdownV2(
                    item.equipmentId(),
                    item.equipmentType(),
                    command.specialTariffId(),
                    specialTariff.getName(),
                    PricingType.SPECIAL.name(),
                    Money.zero(),
                    planned,
                    Duration.ZERO,
                    Duration.ZERO,
                    new BreakdownCostDetails.SpecialGroup()
            ));
        }

        return new BaseRentalCostCalculationResult(
                breakdowns,
                totalCost,
                DiscountDetail.none(),
                totalCost,
                planned,
                false,
                true
        );
    }

    private RentalCostCalculationResult executeNormalMode(RentalCostCalculationV2Command command) {
        LocalDate rentalDate = command.startAt().toLocalDate();
        Duration planned = command.plannedDuration();

        List<EquipmentCostBreakdown> breakdowns = new ArrayList<>();
        Money subtotal = Money.zero();
        boolean anyEstimate = false;
        Map<String, TariffV2> tariffCache = new HashMap<>();

        for (EquipmentCostItemV2 item : command.equipments()) {
            boolean itemIsEstimate = item.returnAt() == null;
            anyEstimate |= itemIsEstimate;

            LocalDateTime itemReturnAt = itemIsEstimate
                    ? command.startAt().plus(planned)
                    : item.returnAt();

            Duration actualDuration = Duration.between(command.startAt(), itemReturnAt);
            Duration overtime;
            Duration billedDuration;
            Duration forgiven;

            Duration overtimeDur = actualDuration.minus(planned);
            if (overtimeDur.isNegative() || overtimeDur.isZero()) {
                billedDuration = actualDuration;
                overtime = Duration.ZERO;
                forgiven = Duration.ZERO;
            } else {
                overtime = overtimeDur;
                long overtimeMinutes = overtimeDur.toMinutes();
                int thresholdMinutes = rentalProperties.getForgivenessThresholdMinutes();
                if (overtimeMinutes <= thresholdMinutes) {
                    billedDuration = planned;
                    forgiven = overtimeDur;
                } else {
                    billedDuration = actualDuration;
                    forgiven = Duration.ZERO;
                }
            }

            TariffV2 tariff = tariffCache.computeIfAbsent(item.equipmentType(),
                    type -> selectTariffUseCase.execute(
                            new SelectTariffV2UseCase.SelectTariffCommand(type, planned, rentalDate)));

            LocalDateTime billingReturnAt = command.startAt().plus(billedDuration);
            RentalCostV2 cost = tariff.calculateCost(command.startAt(), billingReturnAt);

            breakdowns.add(new EquipmentCostBreakdownV2(
                    item.equipmentId(),
                    item.equipmentType(),
                    tariff.getId(),
                    tariff.getName(),
                    tariff.getPricingType().name(),
                    cost.totalCost(),
                    billedDuration,
                    overtime,
                    forgiven,
                    cost.calculationBreakdown()
            ));
            subtotal = subtotal.add(cost.totalCost());
        }

        DiscountPercent discount = Optional.ofNullable(command.discount()).orElse(DiscountPercent.zero());
        Money discountAmount = discount.multiply(subtotal);
        Money totalCost = subtotal.subtract(discountAmount);

        Duration effectiveDuration = anyEstimate
                ? planned
                : breakdowns.stream()
                .map(EquipmentCostBreakdown::billedDuration)
                .max(Comparator.naturalOrder())
                .orElse(planned);

        return new BaseRentalCostCalculationResult(
                breakdowns,
                subtotal,
                new DiscountDetail(discount, discountAmount),
                totalCost,
                effectiveDuration,
                anyEstimate,
                false
        );
    }
}
