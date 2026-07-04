package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.GetAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class GetAgreementTemplateService implements GetAgreementTemplateUseCase {

    private final AgreementTemplateRepository repository;

    GetAgreementTemplateService(AgreementTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    public AgreementTemplate execute(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AgreementTemplate.class, id.toString()));
    }
}
