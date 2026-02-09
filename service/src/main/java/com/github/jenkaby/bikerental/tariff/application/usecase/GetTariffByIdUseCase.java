package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;

import java.util.Optional;

public interface GetTariffByIdUseCase {

    Optional<Tariff> execute(Long id);


    Tariff get(Long id);
}
