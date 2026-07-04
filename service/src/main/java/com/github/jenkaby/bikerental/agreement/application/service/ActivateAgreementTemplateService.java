package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.ActivateAgreementTemplateUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@Service
class ActivateAgreementTemplateService implements ActivateAgreementTemplateUseCase {

    private final AgreementTemplateRepository repository;
    private final ContentHasher contentHasher;
    private final Clock clock;

    ActivateAgreementTemplateService(AgreementTemplateRepository repository,
                                     ContentHasher contentHasher,
                                     Clock clock) {
        this.repository = repository;
        this.contentHasher = contentHasher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AgreementTemplate execute(Long id) {
        var draft = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(AgreementTemplate.class, id.toString()));

        Instant now = clock.instant();

        repository.findActive().ifPresent(current -> {
            current.deactivate(now);
            repository.saveNow(current);
            log.info("Deactivated previously active agreement template {}", current.getId());
        });

        draft.activate(repository.nextVersionNumber(), contentHasher.sha256(draft.getContent()), now);
        var activated = repository.save(draft);
        log.info("Activated agreement template {} as version {}", activated.getId(), activated.getVersionNumber());
        return activated;
    }
}
