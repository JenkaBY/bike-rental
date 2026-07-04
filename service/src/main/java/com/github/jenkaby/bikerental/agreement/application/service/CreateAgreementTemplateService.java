package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.CreateAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
class CreateAgreementTemplateService implements CreateAgreementTemplateUseCase {

    private final AgreementTemplateRepository repository;

    CreateAgreementTemplateService(AgreementTemplateRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public AgreementTemplate execute(CreateAgreementTemplateCommand command) {
        var draft = AgreementTemplate.createDraft(command.title(), command.content());
        var saved = repository.save(draft);
        log.info("Created agreement template draft {}", saved.getId());
        return saved;
    }
}
