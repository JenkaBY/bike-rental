package com.github.jenkaby.bikerental.agreement.infrastructure.pdf;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateVariable;
import com.github.jenkaby.bikerental.agreement.domain.service.AgreementContentRenderer;
import com.github.jenkaby.bikerental.shared.application.service.MessageService;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component

class AgreementPlaceholderRenderer implements AgreementContentRenderer {

    private final AgreementPdfProperties properties;
    private final DateTimeFormatter dateTimeFormat;
    private final DateTimeFormatter titleDateFormat;
    private final MessageService messageService;
    private final ZoneId zoneId;

    AgreementPlaceholderRenderer(AgreementPdfProperties properties, MessageService messageService) {
        this.properties = properties;
        this.dateTimeFormat = DateTimeFormatter.ofPattern(properties.dateTimePattern());
        this.titleDateFormat = DateTimeFormatter.ofPattern(properties.datePattern());
        this.zoneId = ZoneId.of(properties.zoneId());
        this.messageService = messageService;
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

    @Override
    public String substituteTitle(AgreementPdfData data) {
        var activatedAt = ZonedDateTime.ofInstant(data.template().getActivatedAt(), zoneId);
        return "%s %s %s".formatted(data.template().getTitle(),
                label("agreement.pdf.label.title-from"),
                activatedAt.format(titleDateFormat));
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

    private String label(String code) {
        return messageService.getMessage(code);
    }
}
