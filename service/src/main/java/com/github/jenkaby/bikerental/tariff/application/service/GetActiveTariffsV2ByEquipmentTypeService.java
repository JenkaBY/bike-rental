package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.tariff.application.usecase.GetActiveTariffsV2ByEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffV2Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class GetActiveTariffsV2ByEquipmentTypeService implements GetActiveTariffsV2ByEquipmentTypeUseCase {

    private final TariffV2Repository repository;

    GetActiveTariffsV2ByEquipmentTypeService(TariffV2Repository repository) {
        this.repository = repository;
    }

    @Override
    public List<TariffV2> execute(String equipmentTypeSlug) {
        return repository.findActiveForEquipmentType(equipmentTypeSlug);
    }
}
