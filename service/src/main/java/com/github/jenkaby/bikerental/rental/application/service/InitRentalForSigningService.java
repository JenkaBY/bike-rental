package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.application.mapper.RentalCreateCommandMapper;
import com.github.jenkaby.bikerental.rental.application.usecase.CreateOrUpdateDraftRentalUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.CreateOrUpdateDraftRentalUseCase.InitDraftCommand;
import com.github.jenkaby.bikerental.rental.application.usecase.InitRentalForSigningUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.PrepareSigningUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.PrepareSigningUseCase.PrepareSigningCommand;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
class InitRentalForSigningService implements InitRentalForSigningUseCase {

    private final CreateOrUpdateDraftRentalUseCase createOrUpdateDraftRentalUseCase;
    private final PrepareSigningUseCase prepareSigningUseCase;
    private final RentalCreateCommandMapper mapper;

    @Override
    @Transactional
    public Rental execute(InitRentalForSigningCommand command) {
        Rental draft = createOrUpdateDraftRentalUseCase.execute(mapper.toInitDraftCommand(command));
        log.debug("Rental {} draft created directly in AWAITING_SIGNATURE", draft.getId());
        Rental awaitingSignature = prepareSigningUseCase.execute(
                new PrepareSigningCommand(draft.getId(), command.operatorId()));
        log.info("Rental {} created directly in AWAITING_SIGNATURE", awaitingSignature.getId());
        return awaitingSignature;
    }
}
