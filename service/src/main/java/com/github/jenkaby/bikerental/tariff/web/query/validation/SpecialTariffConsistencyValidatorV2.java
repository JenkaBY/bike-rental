package com.github.jenkaby.bikerental.tariff.web.query.validation;

import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationV2Request;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SpecialTariffConsistencyValidatorV2
        implements ConstraintValidator<SpecialTariffConsistency, CostCalculationV2Request> {

    @Override
    public boolean isValid(CostCalculationV2Request value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        boolean isTariffIdProvided = value.specialTariffId() != null;
        boolean isPriceProvided = value.specialPrice() != null;
        return isTariffIdProvided == isPriceProvided;
    }
}
