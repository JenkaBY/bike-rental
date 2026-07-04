package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindRentalSignaturesUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignatureSummary;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementSignatureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
class FindRentalSignaturesService implements FindRentalSignaturesUseCase {

    private final AgreementSignatureRepository signatureRepository;

    FindRentalSignaturesService(AgreementSignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    @Override
    public List<AgreementSignatureSummary> execute(Long rentalId) {
        log.info("Listing signatures for rental {}", rentalId);
        return signatureRepository.findSummaryByRentalId(rentalId)
                .map(List::of)
                .orElse(List.of());
    }
}
