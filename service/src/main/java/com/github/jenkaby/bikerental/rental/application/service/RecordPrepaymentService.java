package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.finance.FinanceFacade;
import com.github.jenkaby.bikerental.finance.PaymentInfo;
import com.github.jenkaby.bikerental.rental.application.usecase.RecordPrepaymentUseCase;
import com.github.jenkaby.bikerental.rental.domain.exception.InsufficientPrepaymentException;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RecordPrepaymentService implements RecordPrepaymentUseCase {

    private final RentalRepository rentalRepository;
    private final FinanceFacade financeFacade;

    RecordPrepaymentService(RentalRepository rentalRepository, FinanceFacade financeFacade) {
        this.rentalRepository = rentalRepository;
        this.financeFacade = financeFacade;
    }

    @Override
    @Transactional
    public PaymentInfo execute(RecordPrepaymentCommand command) {
        Rental rental = rentalRepository.findById(command.rentalId())
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, command.rentalId().toString()));

        if (rental.getStatus() != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(rental.getStatus(), RentalStatus.DRAFT);
        }

        if (!rental.isPrepaymentSufficient(command.amount())) {
            throw InsufficientPrepaymentException.forInsufficientPrepayment(rental);
        }

        return financeFacade.recordPrepayment(
                command.rentalId(),
                command.amount(),
                command.paymentMethod(),
                command.operatorId()
        );
    }
}
