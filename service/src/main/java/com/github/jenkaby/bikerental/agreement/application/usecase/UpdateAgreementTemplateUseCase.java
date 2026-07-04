package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;

public interface UpdateAgreementTemplateUseCase {

    AgreementTemplate execute(UpdateAgreementTemplateCommand command);

    record UpdateAgreementTemplateCommand(Long id, String title, String content) {
    }
}
