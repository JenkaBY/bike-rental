package com.github.jenkaby.bikerental.agreement.domain.repository;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignature;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;

import java.util.Optional;

public interface AgreementSignatureRepository {

    AgreementSignature save(AgreementSignature signature);

    boolean existsByRentalId(Long rentalId);

    Optional<AgreementSignatureSummary> findSummaryByRentalId(Long rentalId);

    // TODO Remove pdf from the method name name. Make it abstract
    Optional<byte[]> findPdfByRentalId(Long rentalId);
}
