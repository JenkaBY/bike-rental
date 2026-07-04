package com.github.jenkaby.bikerental.agreement.domain.repository;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary;

import java.util.List;
import java.util.Optional;

public interface AgreementTemplateRepository {

    AgreementTemplate save(AgreementTemplate template);

    AgreementTemplate saveNow(AgreementTemplate template);

    Optional<AgreementTemplate> findById(Long id);

    Optional<AgreementTemplate> findActive();

    List<AgreementTemplateSummary> findAllSummaries();

    int nextVersionNumber();

    void deleteById(Long id);
}
