package com.github.jenkaby.bikerental.agreement.application.usecase;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;

public interface CreateAgreementTemplateUseCase {

    AgreementTemplate execute(CreateAgreementTemplateCommand command);

    record CreateAgreementTemplateCommand(String title, String content) {
    }
}
