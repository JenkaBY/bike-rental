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
        var violations = delegate.collectViolations(req.pricingType(), req.params());
        if (violations.isEmpty()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        for (var violation : violations) {
            context.buildConstraintViolationWithTemplate(violation.message())
                    .addPropertyNode("params")
                    .addPropertyNode(violation.field())
                    .addConstraintViolation();
        }
        return false;
    }
}
