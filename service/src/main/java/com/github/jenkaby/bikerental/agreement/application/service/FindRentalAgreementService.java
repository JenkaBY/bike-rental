package com.github.jenkaby.bikerental.agreement.application.service;

import com.github.jenkaby.bikerental.agreement.application.usecase.FindRentalAgreementUseCase;
import com.github.jenkaby.bikerental.agreement.domain.exception.ActiveAgreementTemplateNotFoundException;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementPdfData;
import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplate;
import com.github.jenkaby.bikerental.agreement.domain.model.RentalAgreementView;
import com.github.jenkaby.bikerental.agreement.domain.repository.AgreementTemplateRepository;
import com.github.jenkaby.bikerental.agreement.domain.service.AgreementContentRenderer;
import com.github.jenkaby.bikerental.customer.CustomerFacade;
import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.rental.RentalSigningFacade;
import com.github.jenkaby.bikerental.rental.RentalSigningSnapshot;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Slf4j
@Service
class FindRentalAgreementService implements FindRentalAgreementUseCase {

    private final RentalSigningFacade rentalSigningFacade;
    private final AgreementTemplateRepository templateRepository;
    private final CustomerFacade customerFacade;
    private final SigningAssemblyMapper assemblyMapper;
    private final AgreementContentRenderer contentRenderer;
    private final Clock clock;

    FindRentalAgreementService(RentalSigningFacade rentalSigningFacade,
                               AgreementTemplateRepository templateRepository,
                               CustomerFacade customerFacade,
                               SigningAssemblyMapper assemblyMapper,
                               AgreementContentRenderer contentRenderer,
                               Clock clock) {
        this.rentalSigningFacade = rentalSigningFacade;
        this.templateRepository = templateRepository;
        this.customerFacade = customerFacade;
        this.assemblyMapper = assemblyMapper;
        this.contentRenderer = contentRenderer;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public RentalAgreementView execute(Long rentalId) {
        log.info("Rendering agreement text for rental {}", rentalId);

        RentalSigningSnapshot snapshot = rentalSigningFacade.getSigningSnapshot(rentalId);

        AgreementTemplate template = templateRepository.findActive()
                .orElseThrow(ActiveAgreementTemplateNotFoundException::new);

        CustomerInfo customer = customerFacade.findById(snapshot.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(CustomerInfo.class, snapshot.customerId().toString()));

        AgreementPdfData pdfData = assemblyMapper.toPdfData(template, customer, snapshot, clock.instant(), null);
        String content = contentRenderer.substitute(pdfData);

        return new RentalAgreementView(template.getId(), template.getVersionNumber(), template.getTitle(), content);
    }
}
