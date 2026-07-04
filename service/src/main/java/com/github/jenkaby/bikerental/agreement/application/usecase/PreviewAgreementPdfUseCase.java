package com.github.jenkaby.bikerental.agreement.application.usecase;

public interface PreviewAgreementPdfUseCase {

    byte[] execute(PreviewAgreementPdfCommand command);

    record PreviewAgreementPdfCommand(String title, String content) {
    }
}
