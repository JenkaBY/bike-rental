package com.github.jenkaby.bikerental.tariff.web.command.validation;

import com.github.jenkaby.bikerental.shared.domain.utils.MessageCode;
import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.shared.utils.TariffV2FieldNames;
import com.github.jenkaby.bikerental.tariff.web.error.InvalidTariffPricingException;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingParams;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
public class TariffV2PricingValidator {

    private static final String INAPPLICABLE_FIELD = MessageCode.INAPPLICABLE_FIELD;
    private static final String REQUIRED_FIELD_MISSING = MessageCode.REQUIRED_FIELD_MISSING;
    private static final String CONSTRAINT_VIOLATION = MessageCode.CONSTRAINT_VIOLATION;

    public void validate(PricingType pricingType, PricingParams dto) {
        var firstHourPrice = dto.firstHourPrice();
        var hourlyDiscount = dto.hourlyDiscount();
        var minimumHourlyPrice = dto.minimumHourlyPrice();
        var hourlyPrice = dto.hourlyPrice();
        var dailyPrice = dto.dailyPrice();
        var overtimeHourlyPrice = dto.overtimeHourlyPrice();
        var issuanceFee = dto.issuanceFee();
        var minimumDurationMinutes = dto.minimumDurationMinutes();
        var minimumDurationSurcharge = dto.minimumDurationSurcharge();

        switch (pricingType) {
            case DEGRESSIVE_HOURLY -> {
                requirePositive(TariffV2FieldNames.FIRST_HOUR_PRICE, firstHourPrice);
                requirePositive(TariffV2FieldNames.HOURLY_DISCOUNT, hourlyDiscount);
                requirePositive(TariffV2FieldNames.MINIMUM_HOURLY_PRICE, minimumHourlyPrice);
                if (minimumHourlyPrice.compareTo(firstHourPrice) > 0) {
                    throw new InvalidTariffPricingException(TariffV2FieldNames.MINIMUM_HOURLY_PRICE + " must be <= " + TariffV2FieldNames.FIRST_HOUR_PRICE,
                            CONSTRAINT_VIOLATION + "." + TariffV2FieldNames.MINIMUM_HOURLY_PRICE);
                }
                requirePositive(TariffV2FieldNames.MINIMUM_DURATION_MINUTES, minimumDurationMinutes);
                requirePositive(TariffV2FieldNames.MINIMUM_DURATION_SURCHARGE, minimumDurationSurcharge);
            }
            case FLAT_HOURLY -> {
                requirePositive(TariffV2FieldNames.HOURLY_PRICE, hourlyPrice);
                requirePositive(TariffV2FieldNames.MINIMUM_DURATION_MINUTES, minimumDurationMinutes);
                requirePositive(TariffV2FieldNames.MINIMUM_DURATION_SURCHARGE, minimumDurationSurcharge);

            }
            case DAILY -> {
                requirePositive(TariffV2FieldNames.DAILY_PRICE, dailyPrice);
                requirePositive(TariffV2FieldNames.OVERTIME_HOURLY_PRICE, overtimeHourlyPrice);
            }
            case FLAT_FEE -> {
                requireNonNegative(TariffV2FieldNames.ISSUANCE_FEE, issuanceFee);
            }
            case SPECIAL -> {
//                ignore any fields
            }
        }
    }

    private static void requirePositive(String field, Integer value) {
        if (isNull(value) || value <= 0) {
            throw new InvalidTariffPricingException(field + " is required and must be > 0",
                    REQUIRED_FIELD_MISSING + "." + field);
        }
    }

    private static void requirePositive(String field, BigDecimal value) {
        if (isNull(value) || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTariffPricingException(field + " is required and must be > 0",
                    REQUIRED_FIELD_MISSING + "." + field);
        }
    }

    private static void requireNonNegative(String field, BigDecimal value) {
        if (isNull(value) || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTariffPricingException(field + " is required and must be >= 0",
                    REQUIRED_FIELD_MISSING + "." + field);
        }
    }

    private static void requireNull(String field, Object value) {
        if (nonNull(value)) {
            throw new InvalidTariffPricingException(field + " must be null for this pricing type",
                    INAPPLICABLE_FIELD + "." + field);
        }
    }

    private static void requireNull(String field, Integer value) {
        if (nonNull(value)) {
            throw new InvalidTariffPricingException(field + " must be null for this pricing type",
                    INAPPLICABLE_FIELD + "." + field);
        }
    }
}
