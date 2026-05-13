package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface CreateOrUpdateDraftRentalUseCase {


    Rental execute(UpdateDraftRentalCommand command);

    Rental execute(CreateDraftCommand command);

    record UpdateDraftRentalCommand(
            Long rentalId,
            UUID customerId,
            List<Long> equipmentIds,
            Duration duration,
            String operatorId,
            Long specialTariffId,
            Money specialPrice,
            DiscountPercent discountPercent
    ) {
    }

    record CreateDraftCommand() {
    }
}
