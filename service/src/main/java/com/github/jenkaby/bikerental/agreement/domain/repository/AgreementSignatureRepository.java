package com.github.jenkaby.bikerental.agreement.domain.repository;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignature;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;

import java.util.Optional;

public interface AgreementSignatureRepository {

    AgreementSignature save(AgreementSignature signature);

    boolean existsByRentalId(Long rentalId);

    Optional<AgreementSignatureSummary> findSummaryByRentalId(Long rentalId);

    Optional<byte[]> findPdfByRentalId(Long rentalId);
}
