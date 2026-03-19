package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;

import java.util.Optional;

public interface GetTariffV2ByIdUseCase {

    Optional<TariffV2> execute(Long id);

    TariffV2 get(Long id);
}
