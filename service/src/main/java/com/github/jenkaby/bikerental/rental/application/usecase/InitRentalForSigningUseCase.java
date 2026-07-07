package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface InitRentalForSigningUseCase {

    Rental execute(InitRentalForSigningCommand command);

    record InitRentalForSigningCommand(
            UUID customerId,
            List<Long> equipmentIds,
            Duration duration,
            String operatorId,
            Long specialTariffId,
            Money specialPrice,
            DiscountPercent discountPercent
    ) {
    }
}
