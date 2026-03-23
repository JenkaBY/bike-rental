package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;

import java.util.List;

public interface GetActiveTariffsV2ByEquipmentTypeUseCase {
    List<TariffV2> execute(String equipmentTypeSlug);
}
