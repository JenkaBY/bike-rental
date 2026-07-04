package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.DeleteAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
class DeleteAgreementTemplateService implements DeleteAgreementTemplateUseCase {

    private final AgreementTemplateRepository repository;

    DeleteAgreementTemplateService(AgreementTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void execute(Long id) {
        var template = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AgreementTemplate.class, id.toString()));
        template.ensureDeletable();
        repository.deleteById(id);
        log.info("Deleted agreement template {}", id);
    }
}
