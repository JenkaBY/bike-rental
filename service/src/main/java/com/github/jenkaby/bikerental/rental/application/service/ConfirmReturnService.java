package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.application.mapper.RentalEquipmentCostBreakdownMapper;
import com.github.jenkaby.bikerental.rental.application.service.validator.QuoteConsistencyValidator;
import com.github.jenkaby.bikerental.rental.application.usecase.ConfirmReturnUseCase;
import com.github.jenkaby.bikerental.rental.application.usecase.ReturnEquipmentUseCase.ReturnEquipmentResult;
import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.tariff.RentalCostQuote;
import com.github.jenkaby.bikerental.tariff.TariffV2Facade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
class ConfirmReturnService implements ConfirmReturnUseCase {

    private final RentalRepository rentalRepository;
    private final TariffV2Facade tariffFacade;
    private final QuoteConsistencyValidator quoteConsistencyValidator;
    private final RentalDurationCalculator durationCalculator;
    private final RentalEquipmentCostBreakdownMapper breakdownMapper;
    private final RentalSettlementFinalizer settlementFinalizer;

    @Override
    @Transactional
    public @NonNull ReturnEquipmentResult execute(@NonNull ConfirmReturnCommand command) {
        log.info("Confirming return for rentalId={} against {}", command.rentalId(), command.quoteId());

        Rental rental = findRental(command.rentalId());
        if (!rental.hasActiveStatus()) {
            throw new InvalidRentalStatusException(rental.getStatus(), RentalStatus.ACTIVE);
        }

        RentalCostQuote quote = tariffFacade.getQuote(command.quoteId());
        quoteConsistencyValidator.validate(rental, quote);
        tariffFacade.consumeQuote(command.quoteId());

        LocalDateTime frozenReturnTime = quote.resolveReturnTime();
        rental.calculateActualDuration(durationCalculator, frozenReturnTime);
        rental.equipmentsToReturn(quote.equipmentIds(), List.of(), frozenReturnTime);
        rental.applyFinalCost(breakdownMapper.toFrozenCostResults(quote.inputs().equipments().size(), quote.result().equipmentBreakdowns()));

        Money totalFinalCost = quote.result().totalCost();
        assertChargeMatchesQuote(rental, totalFinalCost);
        log.info("Rental [{}] confirming final cost [{}] from [{}]", rental.getId(), totalFinalCost, quote.quoteId());

        return settlementFinalizer.settleAndComplete(rental, totalFinalCost, command.operatorId(), frozenReturnTime);
    }

    private Rental findRental(Long rentalId) {
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, String.valueOf(rentalId)));
    }

    private void assertChargeMatchesQuote(Rental rental, Money frozenTotal) {
        Money recomputed = rental.getFinalCost();
        if (!recomputed.isEqualTo(frozenTotal)) {
            throw new IllegalStateException(
                    "Quoted total %s does not match rental recomputed total %s".formatted(frozenTotal, recomputed));
        }
    }
}
