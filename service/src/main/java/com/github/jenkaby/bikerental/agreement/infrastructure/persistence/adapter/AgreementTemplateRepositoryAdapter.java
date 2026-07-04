package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateSummary;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.mapper.AgreementTemplateJpaMapper;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.repository.AgreementTemplateJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
class AgreementTemplateRepositoryAdapter implements AgreementTemplateRepository {

    private final AgreementTemplateJpaRepository repository;
    private final AgreementTemplateJpaMapper mapper;

    AgreementTemplateRepositoryAdapter(AgreementTemplateJpaRepository repository, AgreementTemplateJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AgreementTemplate save(AgreementTemplate template) {
        var entity = mapper.toEntity(template);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional
    public AgreementTemplate saveNow(AgreementTemplate template) {
        var entity = mapper.toEntity(template);
        var saved = repository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<AgreementTemplate> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<AgreementTemplate> findActive() {
        return repository.findByStatus(AgreementTemplateStatus.ACTIVE).map(mapper::toDomain);
    }

    @Override
    public List<AgreementTemplateSummary> findAllSummaries() {
        return repository.findAllSummaries().stream()
                .map(mapper::toSummary)
                .toList();
    }

    @Override
    public int nextVersionNumber() {
        return repository.findMaxVersionNumber() + 1;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
