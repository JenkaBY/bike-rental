package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.PreviewAgreementPdfUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;
import com.github.jenkaby.bikerental.agreement.domain.service.AgreementPdfRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
class PreviewAgreementPdfService implements PreviewAgreementPdfUseCase {

    private static final AgreementPdfData.CustomerData FIXTURE_CUSTOMER =
            new AgreementPdfData.CustomerData("Иван", "Иванов", "+375291234567");
    private static final Long FIXTURE_RENTAL_ID = 0L;
    private static final Duration FIXTURE_DURATION = Duration.ofHours(2);
    private static final List<AgreementPdfData.EquipmentLine> FIXTURE_EQUIPMENTS = List.of(
            new AgreementPdfData.EquipmentLine("BIKE-001", "Горный велосипед", new BigDecimal("25.00")),
            new AgreementPdfData.EquipmentLine("HELM-014", "Шлем защитный", new BigDecimal("5.00")));

    private final AgreementPdfRenderer renderer;
    private final Clock clock;

    PreviewAgreementPdfService(AgreementPdfRenderer renderer, Clock clock) {
        this.renderer = renderer;
        this.clock = clock;
    }

    @Override
    public byte[] execute(PreviewAgreementPdfCommand command) {
        log.info("Rendering agreement preview PDF for title '{}'", command.title());
        var rental = new AgreementPdfData.RentalData(
                FIXTURE_RENTAL_ID,
                LocalDateTime.now(clock),
                FIXTURE_DURATION,
                FIXTURE_EQUIPMENTS);
        var data = new AgreementPdfData(
                command.title(),
                command.content(),
                FIXTURE_CUSTOMER,
                rental,
                null);
        return renderer.render(data);
    }
}
