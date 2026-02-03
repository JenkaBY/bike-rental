package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;

import java.util.List;

public interface GetActiveTariffsByEquipmentTypeUseCase {
    List<Tariff> execute(String equipmentTypeSlug);
}
