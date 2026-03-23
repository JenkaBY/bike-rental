package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.tariff.SuitableTariffNotFoundException;
import com.github.jenkaby.bikerental.tariff.application.usecase.SelectTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffV2Repository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

@Service
class SelectTariffV2Service implements SelectTariffV2UseCase {

    private final TariffV2Repository repository;
    private final Clock clock;

    SelectTariffV2Service(TariffV2Repository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public TariffV2 execute(SelectTariffCommand command) {
        var date = Optional.ofNullable(command.rentalDate()).orElse(LocalDate.now(clock));
        return repository.findActiveByEquipmentTypeAndValidOn(command.equipmentTypeSlug(), date).stream()
                .min(Comparator.comparing(tariff -> tariff.calculateCost(command.duration()).totalCost()))
                .orElseThrow(() -> new SuitableTariffNotFoundException(command.equipmentTypeSlug(), date, command.duration()));
    }
}
