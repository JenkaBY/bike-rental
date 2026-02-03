package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;

public interface ActivateTariffUseCase {
    Tariff execute(Long id);
}
