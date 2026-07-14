package com.github.jenkaby.bikerental.rental.application.service.validator;

import com.github.jenkaby.bikerental.rental.domain.exception.QuoteRentalMismatchException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.tariff.EquipmentCostItemV2;
import com.github.jenkaby.bikerental.tariff.RentalCostQuote;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Component
public class QuoteConsistencyValidator {

    public void validate(Rental rental, RentalCostQuote quote) {
        var inputs = quote.inputs();

        if (quote.result().estimate()) {
            throw mismatch(quote, rental, "quote is an estimate and cannot settle a final return");
        }

        var quoteStartedAt = inputs.startAt();
        var rentalStartedAt = rental.getStartedAt();
        if (!temporalEquals(quoteStartedAt, rentalStartedAt)) {
            log.warn("Rental[{}] start time {} differs from the quote {} quoteId=[{}]",
                    rental.getId(), rentalStartedAt, quoteStartedAt, quote.quoteId().id());
            throw mismatch(quote, rental, "rental start time differs from the rental");
        }

        if (!Objects.equals(inputs.plannedDuration(), rental.getPlannedDuration())) {
            log.warn("Rental[{}] planned duration {} differs from the quote {} quoteId=[{}]",
                    rental.getId(), rental.getPlannedDuration(), inputs.plannedDuration(), quote.quoteId().id());
            throw mismatch(quote, rental, "planned duration differs from the rental");
        }

        if (!Objects.equals(inputs.discount(), rental.getDiscountPercent())) {
            log.warn("Rental[{}] discount {} differs from the quote {} quoteId=[{}]",
                    rental.getId(), rental.getDiscountPercent(), inputs.discount(), quote.quoteId().id());
            throw mismatch(quote, rental, "discount differs from the rental");
        }

        if (!Objects.equals(inputs.specialTariffId(), rental.getSpecialTariffId())) {
            log.warn("Rental[{}] special tariff {} differs from the quote {} quoteId=[{}]",
                    rental.getId(), rental.getSpecialTariffId(), inputs.specialTariffId(), quote.quoteId().id());
            throw mismatch(quote, rental, "special tariff differs from the rental");
        }

        if (!moneyEquals(inputs.specialPrice(), rental.getSpecialPrice())) {
            log.warn("Rental[{}] special price {} differs from the quote {} quoteId=[{}]",
                    rental.getId(), rental.getSpecialPrice(), inputs.specialPrice(), quote.quoteId().id());
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
            log.warn("Rental[{}] equipment composition {} differs from the quote {} quoteId=[{}]",
                    rental.getId(), equipmentsById.keySet(), quotedEquipmentIds, quote.quoteId().id());
            throw mismatch(quote, rental, "equipment composition differs from the rental");
        }

        for (EquipmentCostItemV2 item : items) {
            var equipment = equipmentsById.get(item.equipmentId());
            if (!temporalEquals(equipment.getStartedAt(), item.resolveStartAt(quote.inputs().startAt()))) {
                log.warn("Rental[{}] equipment {} start time {} differs from the quote {} quoteId=[{}]",
                        rental.getId(), item.equipmentId(), equipment.getStartedAt(), item.resolveStartAt(quote.inputs().startAt()), quote.quoteId().id());
                throw mismatch(quote, rental,
                        "equipment %d start time differs from the rental".formatted(item.equipmentId()));
            }
            if (equipment.getActualReturnAt() != null && !temporalEquals(equipment.getActualReturnAt(), item.returnAt())) {
                log.warn("Rental[{}] equipment {} return time {} differs from the quote {} quoteId=[{}]",
                        rental.getId(), item.equipmentId(), equipment.getActualReturnAt(), item.returnAt(), quote.quoteId().id());
                throw mismatch(quote, rental,
                        "equipment %d was already returned at a different time than quoted".formatted(item.equipmentId()));
            }
        }
    }

    private QuoteRentalMismatchException mismatch(RentalCostQuote quote, Rental rental, String reason) {
        return new QuoteRentalMismatchException(quote.quoteId().id(), rental.getId(), reason);
    }

    private static boolean moneyEquals(@Nullable Money left, @Nullable Money right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.isEqualTo(right);
    }

    private static boolean temporalEquals(@Nullable LocalDateTime left, @Nullable LocalDateTime right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.truncatedTo(ChronoUnit.SECONDS).isEqual(right.truncatedTo(ChronoUnit.SECONDS));
    }
}
