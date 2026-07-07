package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.rental.application.usecase.CancelSigningUseCase;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
class CancelSigningService implements CancelSigningUseCase {

    private final RentalRepository rentalRepository;
    private final FinanceFacade financeFacade;

    CancelSigningService(RentalRepository rentalRepository, FinanceFacade financeFacade) {
        this.rentalRepository = rentalRepository;
        this.financeFacade = financeFacade;
    }

    @Override
    @Transactional
    public Rental execute(CancelSigningCommand command) {
        Rental rental = rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));

        rental.cancelSigning();

        if (financeFacade.hasHold(rental.toRentalRef())) {
            log.info("Releasing hold for cancelled signing of rental {}", rental.getId());
            financeFacade.releaseHold(rental.toActualRentalRef(), command.operatorId());
        }

        Rental saved = rentalRepository.save(rental);
        log.info("Rental {} moved back to DRAFT (signing cancelled)", saved.getId());
        return saved;
    }
}
