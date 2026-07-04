package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.UpdateAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
class UpdateAgreementTemplateService implements UpdateAgreementTemplateUseCase {

    private final AgreementTemplateRepository repository;

    UpdateAgreementTemplateService(AgreementTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public AgreementTemplate execute(UpdateAgreementTemplateCommand command) {
        var template = repository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException(AgreementTemplate.class, command.id().toString()));
        template.updateContent(command.title(), command.content());
        var saved = repository.save(template);
        log.info("Updated agreement template {}", saved.getId());
        return saved;
    }
}
