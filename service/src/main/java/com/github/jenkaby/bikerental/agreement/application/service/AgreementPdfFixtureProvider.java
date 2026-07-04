package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
class AgreementPdfFixtureProvider {

    private static final AgreementPdfData.CustomerData FIXTURE_CUSTOMER =
            new AgreementPdfData.CustomerData("Иван", "Иванов", "+375291234567");
    private static final Long FIXTURE_RENTAL_ID = 0L;
    private static final Duration FIXTURE_DURATION = Duration.ofHours(2);
//    TODO we need to pass money as well. The default is BYN. Take them from application.properties
    private static final List<AgreementPdfData.EquipmentLine> FIXTURE_EQUIPMENTS = List.of(
            new AgreementPdfData.EquipmentLine("BIKE-001", "Горный велосипед", new BigDecimal("25.00")),
            new AgreementPdfData.EquipmentLine("HELM-014", "Шлем защитный", new BigDecimal("5.00")));
    private static final BigDecimal FIXTURE_ESTIMATED_TOTAL = new BigDecimal("30.00");

    private final Clock clock;

    AgreementPdfFixtureProvider(Clock clock) {
        this.clock = clock;
    }

    AgreementPdfData previewData(String title, String content) {
        var rental = new AgreementPdfData.RentalData(
                FIXTURE_RENTAL_ID,
                LocalDateTime.now(clock),
                FIXTURE_DURATION,
                FIXTURE_EQUIPMENTS,
                FIXTURE_ESTIMATED_TOTAL,
                null,
                null);
        return new AgreementPdfData(title, content, FIXTURE_CUSTOMER, rental, null);
    }
}
