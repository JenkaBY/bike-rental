package com.github.jenkaby.bikerental.agreement.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignature;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementSignatureRepository;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.mapper.AgreementSignatureJpaMapper;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.repository.AgreementSignatureJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
class AgreementSignatureRepositoryAdapter implements AgreementSignatureRepository {

    private final AgreementSignatureJpaRepository repository;
    private final AgreementSignatureJpaMapper mapper;

    AgreementSignatureRepositoryAdapter(AgreementSignatureJpaRepository repository,
                                        AgreementSignatureJpaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public AgreementSignature save(AgreementSignature signature) {
        var entity = mapper.toEntity(signature);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public boolean existsByRentalId(Long rentalId) {
        return repository.existsByRentalId(rentalId);
    }

    @Override
    public Optional<AgreementSignatureSummary> findSummaryByRentalId(Long rentalId) {
        return repository.findSummaryByRentalId(rentalId).map(mapper::toSummary);
    }

    @Override
    public Optional<byte[]> findDocumentByRentalId(Long rentalId) {
        return repository.findPdfByRentalId(rentalId);
    }
}
