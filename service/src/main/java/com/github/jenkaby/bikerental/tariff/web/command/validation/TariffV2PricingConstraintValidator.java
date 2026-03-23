package com.github.jenkaby.bikerental.tariff.web.command.validation;

import com.github.jenkaby.bikerental.tariff.web.command.dto.TariffV2Request;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TariffV2PricingConstraintValidator implements ConstraintValidator<ValidTariffV2Pricing, TariffV2Request> {

    private final TariffV2PricingValidator delegate;

    @Override
    public boolean isValid(TariffV2Request req, ConstraintValidatorContext context) {
        if (req == null) {
            return true;
        }
        try {
            delegate.validate(req.pricingType(), req.params());
        } catch (RuntimeException ex) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ex.getMessage())
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}