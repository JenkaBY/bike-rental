package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.config.RentalProperties;
import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.*;
import com.github.jenkaby.bikerental.tariff.application.usecase.RentalCostCalculationUseCase;
import com.github.jenkaby.bikerental.tariff.application.usecase.SelectTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.domain.exception.InvalidSpecialPriceException;
import com.github.jenkaby.bikerental.tariff.domain.exception.InvalidSpecialTariffTypeException;
import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffV2Repository;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseEquipmentCostBreakdown;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseRentalCostCalculationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
class RentalCostCalculationService implements RentalCostCalculationUseCase {

    private final RentalProperties rentalProperties;
    private final TariffV2Repository tariffRepository;
    private final SelectTariffV2UseCase selectTariffUseCase;
    private final Clock clock;

    RentalCostCalculationService(RentalProperties rentalProperties,
                                 TariffV2Repository tariffRepository,
                                 SelectTariffV2UseCase selectTariffUseCase,
                                 Clock clock) {
        this.rentalProperties = rentalProperties;
        this.tariffRepository = tariffRepository;
        this.selectTariffUseCase = selectTariffUseCase;
        this.clock = clock;
    }

    @Override
    public RentalCostCalculationResult execute(RentalCostCalculationCommand command) {
        if (command.specialTariffId() != null) {
            return executeSpecialMode(command);
        }
        return executeNormalMode(command);
    }

    private RentalCostCalculationResult executeSpecialMode(RentalCostCalculationCommand command) {
        var specialTariff = tariffRepository.get(command.specialTariffId());
        if (specialTariff.getPricingType() != PricingType.SPECIAL) {
            throw new InvalidSpecialTariffTypeException(command.specialTariffId(), specialTariff.getPricingType());
        }
        BigDecimal priceAmount = command.specialPrice();
        if (priceAmount == null || priceAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidSpecialPriceException();
        }

        Money totalCost = Money.of(priceAmount);
        List<EquipmentCostBreakdown> breakdowns = new ArrayList<>();
        Duration effective = command.effectiveDuration();
        for (EquipmentCostItem item : command.equipments()) {
            breakdowns.add(new BaseEquipmentCostBreakdown(
                    item.equipmentTypeSlug(),
                    command.specialTariffId(),
                    specialTariff.getName(),
                    PricingType.SPECIAL.name(),
                    Money.zero(),
                    effective,
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
                effective,
                command.actualDuration() == null,
                true
        );
    }

    private RentalCostCalculationResult executeNormalMode(RentalCostCalculationCommand command) {
        LocalDate rentalDate = Optional.ofNullable(command.rentalDate()).orElse(LocalDate.now(clock));
        Duration planned = command.plannedDuration();
        Duration actual = command.actualDuration();
        boolean estimate = actual == null;
        Duration effective = command.effectiveDuration();
        Duration billedDuration;
        Duration overtime;
        Duration forgiven;

        if (estimate) {
            billedDuration = planned;
            overtime = Duration.ZERO;
            forgiven = Duration.ZERO;
        } else {
            Duration overtimeDur = actual.minus(planned);
            if (overtimeDur.isNegative() || overtimeDur.isZero()) {
                billedDuration = actual;
                overtime = Duration.ZERO;
                forgiven = Duration.ZERO;
            } else {
                overtime = overtimeDur;
                int thresholdMinutes = rentalProperties.getForgivenessThresholdMinutes();
                long overtimeMinutes = overtimeDur.toMinutes();
                if (overtimeMinutes <= thresholdMinutes) {
                    billedDuration = planned;
                    forgiven = overtimeDur;
                } else {
                    billedDuration = actual;
                    forgiven = Duration.ZERO;
                }
            }
        }

        List<EquipmentCostBreakdown> breakdowns = new ArrayList<>();
        Money subtotal = Money.zero();
        Map<String, TariffV2> tariffCache = new HashMap<>();
        for (EquipmentCostItem item : command.equipments()) {
            TariffV2 tariff = tariffCache.computeIfAbsent(item.equipmentTypeSlug(),
                    type -> selectTariffUseCase.execute(new SelectTariffV2UseCase.SelectTariffCommand(item.equipmentTypeSlug(), billedDuration, rentalDate)));
            RentalCostV2 cost = tariff.calculateCost(billedDuration);

            breakdowns.add(new BaseEquipmentCostBreakdown(
                    item.equipmentTypeSlug(),
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

        return new BaseRentalCostCalculationResult(
                breakdowns,
                subtotal,
                new DiscountDetail(discount, discountAmount),
                totalCost,
                effective,
                estimate,
                false
        );
    }
}
