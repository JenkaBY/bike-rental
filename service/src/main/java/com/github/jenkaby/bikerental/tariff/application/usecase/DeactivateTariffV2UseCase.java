package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;

public interface DeactivateTariffV2UseCase {
    TariffV2 execute(Long id);
}
