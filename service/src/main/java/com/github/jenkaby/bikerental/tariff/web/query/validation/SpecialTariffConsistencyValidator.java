package com.github.jenkaby.bikerental.tariff.web.query.validation;

import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SpecialTariffConsistencyValidator implements ConstraintValidator<SpecialTariffConsistency, CostCalculationRequest> {

    @Override
    public boolean isValid(CostCalculationRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        boolean isTariffIdProvided = value.specialTariffId() != null;
        boolean isPriceProvided = value.specialPrice() != null;
        return isTariffIdProvided == isPriceProvided;
    }
}


