package com.github.jenkaby.bikerental.tariff.web.command.validation;

import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.shared.utils.TariffV2FieldNames;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingParams;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@Component
public class TariffV2PricingValidator {

    public record PricingViolation(String field, String message) {}

    public List<PricingViolation> collectViolations(PricingType pricingType, PricingParams dto) {
        if (pricingType == null || dto == null) {
            return List.of();
        }
        List<PricingViolation> violations = new ArrayList<>();

        switch (pricingType) {
            case DEGRESSIVE_HOURLY -> {
                boolean firstHourPriceValid = checkPositive(TariffV2FieldNames.FIRST_HOUR_PRICE, dto.firstHourPrice(), violations);
                checkPositive(TariffV2FieldNames.HOURLY_DISCOUNT, dto.hourlyDiscount(), violations);
                boolean minHourlyPriceValid = checkPositive(TariffV2FieldNames.MINIMUM_HOURLY_PRICE, dto.minimumHourlyPrice(), violations);
                if (firstHourPriceValid && minHourlyPriceValid
                        && dto.minimumHourlyPrice().compareTo(dto.firstHourPrice()) > 0) {
                    violations.add(new PricingViolation(TariffV2FieldNames.MINIMUM_HOURLY_PRICE,
                            TariffV2FieldNames.MINIMUM_HOURLY_PRICE + " must be <= " + TariffV2FieldNames.FIRST_HOUR_PRICE));
                }
                checkPositive(TariffV2FieldNames.MINIMUM_DURATION_MINUTES, dto.minimumDurationMinutes(), violations);
                checkPositive(TariffV2FieldNames.MINIMUM_DURATION_SURCHARGE, dto.minimumDurationSurcharge(), violations);
            }
            case FLAT_HOURLY -> {
                checkPositive(TariffV2FieldNames.HOURLY_PRICE, dto.hourlyPrice(), violations);
                checkPositive(TariffV2FieldNames.MINIMUM_DURATION_MINUTES, dto.minimumDurationMinutes(), violations);
                checkPositive(TariffV2FieldNames.MINIMUM_DURATION_SURCHARGE, dto.minimumDurationSurcharge(), violations);
            }
            case DAILY -> {
                checkPositive(TariffV2FieldNames.DAILY_PRICE, dto.dailyPrice(), violations);
                checkPositive(TariffV2FieldNames.OVERTIME_HOURLY_PRICE, dto.overtimeHourlyPrice(), violations);
            }
            case FLAT_FEE -> {
                checkNonNegative(TariffV2FieldNames.ISSUANCE_FEE, dto.issuanceFee(), violations);
            }
            case SPECIAL -> {
            }
        }

        return violations;
    }

    private static boolean checkPositive(String field, Integer value, List<PricingViolation> violations) {
        if (isNull(value) || value <= 0) {
            violations.add(new PricingViolation(field, field + " is required and must be > 0"));
            return false;
        }
        return true;
    }

    private static boolean checkPositive(String field, BigDecimal value, List<PricingViolation> violations) {
        if (isNull(value) || value.compareTo(BigDecimal.ZERO) <= 0) {
            violations.add(new PricingViolation(field, field + " is required and must be > 0"));
            return false;
        }
        return true;
    }

    private static boolean checkNonNegative(String field, BigDecimal value, List<PricingViolation> violations) {
        if (isNull(value) || value.compareTo(BigDecimal.ZERO) < 0) {
            violations.add(new PricingViolation(field, field + " is required and must be >= 0"));
            return false;
        }
        return true;
    }
}
