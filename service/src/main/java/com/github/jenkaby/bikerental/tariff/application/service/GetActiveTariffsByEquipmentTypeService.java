package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.tariff.application.usecase.GetActiveTariffsByEquipmentTypeUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class GetActiveTariffsByEquipmentTypeService implements GetActiveTariffsByEquipmentTypeUseCase {

    private final TariffRepository repository;

    GetActiveTariffsByEquipmentTypeService(TariffRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Tariff> execute(String equipmentTypeSlug) {
        return repository.findActiveForEquipmentType(equipmentTypeSlug);
    }
}
