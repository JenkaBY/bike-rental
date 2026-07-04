package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindAgreementTemplateSummariesUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class FindAgreementTemplateSummariesService implements FindAgreementTemplateSummariesUseCase {

    private final AgreementTemplateRepository repository;

    FindAgreementTemplateSummariesService(AgreementTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<AgreementTemplateSummary> execute() {
        return repository.findAllSummaries();
    }
}
