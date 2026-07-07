package com.github.jenkaby.bikerental.agreement.infrastructure.pdf;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateVariable;
import com.github.jenkaby.bikerental.agreement.domain.service.AgreementContentRenderer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
class AgreementPlaceholderRenderer implements AgreementContentRenderer {

    private final AgreementPdfProperties properties;
    private final DateTimeFormatter dateTimeFormat;
    private final ZoneId zoneId;

    AgreementPlaceholderRenderer(AgreementPdfProperties properties) {
        this.properties = properties;
        this.dateTimeFormat = DateTimeFormatter.ofPattern(properties.dateTimePattern());
        this.zoneId = ZoneId.of(properties.zoneId());
    }

    @Override
    public String substitute(AgreementPdfData data) {
        ZonedDateTime startedAt = ZonedDateTime.ofInstant(data.rental().startedAt(), zoneId);
        String result = data.template().getContent();
        for (AgreementTemplateVariable variable : AgreementTemplateVariable.values()) {
            result = result.replace(variable.placeholder(), resolveVariable(variable, data, startedAt));
        }
        return result;
    }

    private String resolveVariable(AgreementTemplateVariable variable, AgreementPdfData data, ZonedDateTime startedAt) {
        AgreementPdfData.RentalData rental = data.rental();
        return switch (variable) {
            case CUSTOMER_FIRST_NAME -> data.customer().firstName();
            case CUSTOMER_LAST_NAME -> data.customer().lastName();
            case CUSTOMER_PHONE -> data.customer().phone();
            case RENTAL_STARTED_AT -> startedAt.format(dateTimeFormat);
            case RENTAL_DURATION -> formatDuration(rental.plannedDuration());
            case RENTAL_TOTAL -> rental.estimatedTotal() + " " + properties.currency();
            case RENTAL_NUMBER -> String.valueOf(rental.rentalId());
        };
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }
}
