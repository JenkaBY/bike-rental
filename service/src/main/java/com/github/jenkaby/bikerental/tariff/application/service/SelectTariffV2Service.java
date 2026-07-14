package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.infrastructure.port.clock.TimeProvider;
import com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException;
import com.github.jenkaby.bikerental.tariff.application.usecase.SelectTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffV2Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class SelectTariffV2Service implements SelectTariffV2UseCase {

    private final TariffV2Repository repository;
    private final TimeProvider timeProvider;

    @Override
    public TariffV2 execute(SelectTariffCommand command) {
        var date = Optional.ofNullable(command.rentalDate()).orElse(timeProvider.today());
        LocalDateTime startAt = date.atStartOfDay();
        LocalDateTime returnAt = startAt.plus(command.duration());
        return repository.findActiveByEquipmentTypeAndValidOn(command.equipmentTypeSlug(), date).stream()
                .min(Comparator.comparing(tariff -> tariff.calculateCost(startAt, returnAt).totalCost()))
                .orElseThrow(() -> new SuitableTariffNotFoundException(command.equipmentTypeSlug(), date, command.duration()));
    }
}
