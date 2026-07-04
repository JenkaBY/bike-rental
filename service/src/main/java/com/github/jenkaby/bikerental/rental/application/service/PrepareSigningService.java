package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.usecase.PrepareSigningUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
class PrepareSigningService implements PrepareSigningUseCase {

    private final RentalRepository rentalRepository;
    private final FinanceFacade financeFacade;

    PrepareSigningService(RentalRepository rentalRepository, FinanceFacade financeFacade) {
        this.rentalRepository = rentalRepository;
        this.financeFacade = financeFacade;
    }

    @Override
    @Transactional
    public Rental execute(PrepareSigningCommand command) {
        Rental rental = rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));

        rental.prepareForSigning();

        if (rental.getEstimatedCost().isPositive()) {
            var holdInfo = financeFacade.holdFunds(
                    new CustomerRef(rental.getCustomerId()),
                    rental.toRentalRef(),
                    rental.getEstimatedCost(),
                    command.operatorId());
            log.info("Funds held for signing of rental {}: transactionId={}, heldAt={}",
                    rental.getId(), holdInfo.transactionRef().id(), holdInfo.recordedAt());
        }

        Rental saved = rentalRepository.save(rental);
        log.info("Rental {} moved to AWAITING_SIGNATURE", saved.getId());
        return saved;
    }
}
