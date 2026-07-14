package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.AgreementSignedEvent;
import com.github.jenkaby.bikerental.agreement.application.usecase.SignAgreementUseCase;
import com.github.jenkaby.bikerental.agreement.domain.exception.ActiveAgreementTemplateNotFoundException;
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementAlreadySignedException;
import com.github.jenkaby.bikerental.agreement.domain.exception.AgreementTemplateNotActiveException;
import com.github.jenkaby.bikerental.agreement.domain.exception.SigningVersionMismatchException;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementSignature;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.model.SigningSnapshot;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementSignatureRepository;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import com.github.jenkaby.bikerental.agreement.domain.service.AgreementPdfRenderer;
import com.github.jenkaby.bikerental.customer.CustomerFacade;
import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.rental.RentalSigningFacade;
import com.github.jenkaby.bikerental.rental.RentalSigningSnapshot;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.shared.infrastructure.port.clock.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
class SignAgreementService implements SignAgreementUseCase {

    private static final String AGREEMENT_EVENTS_DESTINATION = "agreement-events";

    private final RentalSigningFacade rentalSigningFacade;
    private final AgreementTemplateRepository templateRepository;
    private final AgreementSignatureRepository signatureRepository;
    private final CustomerFacade customerFacade;
    private final AgreementPdfRenderer renderer;
    private final ContentHasher contentHasher;
    private final SigningAssemblyMapper assemblyMapper;
    private final SignatureImageDecoder signatureImageDecoder;
    private final EventPublisher eventPublisher;
    private final TimeProvider timeProvider;

    @Override
    @Transactional
    public SignAgreementResult execute(SignAgreementCommand command) {
        Long rentalId = command.rentalId();
        log.info("Signing agreement for rental {}", rentalId);

        if (signatureRepository.existsByRentalId(rentalId)) {
            throw new AgreementAlreadySignedException(rentalId);
        }

        RentalSigningSnapshot snapshot = rentalSigningFacade.getSigningSnapshot(rentalId);

        AgreementTemplate template = templateRepository.findActive()
                .orElseThrow(ActiveAgreementTemplateNotFoundException::new);
        if (!template.getId().equals(command.templateId())) {
            throw new AgreementTemplateNotActiveException(command.templateId(), template.getId());
        }

        if (!snapshot.version().equals(command.rentalVersion())) {
            throw new SigningVersionMismatchException(rentalId, command.rentalVersion(), snapshot.version());
        }

        CustomerInfo customer = customerFacade.findById(snapshot.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(CustomerInfo.class, snapshot.customerId().toString()));

        Instant signedAt = timeProvider.nowInstant();
        LocalDateTime startedAt = signedAt.atZone(timeProvider.zoneId()).toLocalDateTime();

        byte[] signaturePng = signatureImageDecoder.decode(command.signaturePngBase64());

        AgreementPdfData pdfData = assemblyMapper.toPdfData(template, customer, snapshot, signedAt, signaturePng);
        byte[] pdfDocument = renderer.render(pdfData);
        String pdfSha256 = contentHasher.sha256(pdfDocument);

        SigningSnapshot signingSnapshot = assemblyMapper.toSigningSnapshot(
                customer, snapshot, startedAt, template.getId(), template.getVersionNumber(), template.getContentSha256());

        AgreementSignature signature = AgreementSignature.create(
                template.getId(),
                rentalId,
                snapshot.customerId(),
                command.operatorId(),
                signingSnapshot,
                pdfDocument,
                pdfSha256,
                template.getContentSha256(),
                signaturePng,
                signedAt,
                command.ipAddress(),
                command.userAgent());
        AgreementSignature saved = signatureRepository.save(signature);

        rentalSigningFacade.completeSigning(rentalId, command.rentalVersion(), signedAt);

        eventPublisher.publish(AGREEMENT_EVENTS_DESTINATION, new AgreementSignedEvent(rentalId, saved.getId()));
        log.info("Signed agreement {} for rental {}", saved.getId(), rentalId);

        return new SignAgreementResult(saved.getId(), signedAt);
    }
}
