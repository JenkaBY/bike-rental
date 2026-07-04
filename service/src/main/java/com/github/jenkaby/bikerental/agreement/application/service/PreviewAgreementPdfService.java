package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.PreviewAgreementPdfUseCase;
import com.github.jenkaby.bikerental.agreement.domain.service.AgreementPdfRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class PreviewAgreementPdfService implements PreviewAgreementPdfUseCase {

    private final AgreementPdfRenderer renderer;
    private final AgreementPdfFixtureProvider fixtureProvider;

    PreviewAgreementPdfService(AgreementPdfRenderer renderer, AgreementPdfFixtureProvider fixtureProvider) {
        this.renderer = renderer;
        this.fixtureProvider = fixtureProvider;
    }

    @Override
    public byte[] execute(PreviewAgreementPdfCommand command) {
        log.info("Rendering agreement preview PDF for title '{}'", command.title());
        var data = fixtureProvider.previewData(command.title(), command.content());
        return renderer.render(data);
    }
}
