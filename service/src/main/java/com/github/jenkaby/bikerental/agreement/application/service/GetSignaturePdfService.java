package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.GetSignaturePdfUseCase;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignature;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementSignatureRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
class GetSignaturePdfService implements GetSignaturePdfUseCase {

    private final AgreementSignatureRepository signatureRepository;

    GetSignaturePdfService(AgreementSignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    @Override
    public byte[] execute(Long rentalId) {
        log.info("Fetching signature PDF for rental {}", rentalId);
        return signatureRepository.findDocumentByRentalId(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException(AgreementSignature.class, rentalId.toString()));
    }
}
