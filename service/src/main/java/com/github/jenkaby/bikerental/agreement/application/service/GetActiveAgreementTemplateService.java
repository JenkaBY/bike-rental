package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.GetActiveAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.exception.ActiveAgreementTemplateNotFoundException;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class GetActiveAgreementTemplateService implements GetActiveAgreementTemplateUseCase {

    private final AgreementTemplateRepository repository;

    GetActiveAgreementTemplateService(AgreementTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public AgreementTemplate execute() {
        return repository.findActive()
                .orElseThrow(ActiveAgreementTemplateNotFoundException::new);
    }
}
