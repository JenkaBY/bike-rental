package com.github.jenkaby.bikerental.rental.application.service.validator;

import com.github.jenkaby.bikerental.rental.domain.exception.QuoteRentalMismatchException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.EquipmentCostItemV2;
import com.github.jenkaby.bikerental.tariff.RentalCostQuote;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class QuoteConsistencyValidator {

    public void validate(Rental rental, RentalCostQuote quote) {
        var inputs = quote.inputs();

        if (quote.result().estimate()) {
            throw mismatch(quote, rental, "quote is an estimate and cannot settle a final return");
        }

        if (!Objects.equals(inputs.startAt(), rental.getStartedAt())) {
            throw mismatch(quote, rental, "rental start time differs from the rental");
        }

        if (!Objects.equals(inputs.plannedDuration(), rental.getPlannedDuration())) {
            throw mismatch(quote, rental, "planned duration differs from the rental");
        }

        if (!Objects.equals(inputs.discount(), rental.getDiscountPercent())) {
            throw mismatch(quote, rental, "discount differs from the rental");
        }

        if (!Objects.equals(inputs.specialTariffId(), rental.getSpecialTariffId())) {
            throw mismatch(quote, rental, "special tariff differs from the rental");
        }

        if (!moneyEquals(inputs.specialPrice(), rental.getSpecialPrice())) {
            throw mismatch(quote, rental, "special price differs from the rental");
        }

        validateEquipmentLines(rental, quote, inputs.equipments());
    }

    private void validateEquipmentLines(Rental rental, RentalCostQuote quote, List<EquipmentCostItemV2> items) {
        var equipmentsById = rental.getEquipments().stream()
                .collect(Collectors.toMap(RentalEquipment::getEquipmentId, Function.identity()));
        var quotedEquipmentIds = items.stream()
                .map(EquipmentCostItemV2::equipmentId)
                .collect(Collectors.toSet());

        if (!equipmentsById.keySet().equals(quotedEquipmentIds)) {
            throw mismatch(quote, rental, "equipment composition differs from the rental");
        }

        for (EquipmentCostItemV2 item : items) {
            var equipment = equipmentsById.get(item.equipmentId());
            if (!Objects.equals(equipment.getStartedAt(), item.startAt())) {
                throw mismatch(quote, rental,
                        "equipment %d start time differs from the rental".formatted(item.equipmentId()));
            }
            if (equipment.getActualReturnAt() != null && !Objects.equals(equipment.getActualReturnAt(), item.returnAt())) {
                throw mismatch(quote, rental,
                        "equipment %d was already returned at a different time than quoted".formatted(item.equipmentId()));
            }
        }
    }

    private QuoteRentalMismatchException mismatch(RentalCostQuote quote, Rental rental, String reason) {
        return new QuoteRentalMismatchException(quote.quoteId(), rental.getId(), reason);
    }

    private static boolean moneyEquals(@Nullable Money left, @Nullable Money right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.isEqualTo(right);
    }
}
