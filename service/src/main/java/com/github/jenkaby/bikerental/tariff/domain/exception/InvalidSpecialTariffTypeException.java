package com.github.jenkaby.bikerental.tariff.domain.exception;

import com.github.jenkaby.bikerental.shared.exception.BikeRentalException;
import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import lombok.Getter;

@Getter
public class InvalidSpecialTariffTypeException extends BikeRentalException {

    public static final String ERROR_CODE = "tariff.special.type_invalid";
    private static final String MESSAGE_TEMPLATE = "Tariff with id %s has pricing type '%s', but SPECIAL is required";

    public InvalidSpecialTariffTypeException(Long tariffId, PricingType actualType) {
        super(MESSAGE_TEMPLATE.formatted(tariffId, actualType), ERROR_CODE, new TariffTypeDetails(tariffId, actualType.name()));
    }

    public TariffTypeDetails getDetails() {
        return getParams()
                .map(p -> (TariffTypeDetails) p)
                .orElseThrow(() -> new IllegalStateException("Expected TariffTypeDetails in exception parameters"));
    }

    public record TariffTypeDetails(Long tariffId, String actualPricingType) {
    }
}

