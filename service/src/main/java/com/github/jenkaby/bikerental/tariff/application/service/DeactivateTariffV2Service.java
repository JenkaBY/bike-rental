package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.tariff.application.usecase.DeactivateTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffV2Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DeactivateTariffV2Service implements DeactivateTariffV2UseCase {

    private final TariffV2Repository repository;

    DeactivateTariffV2Service(TariffV2Repository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public TariffV2 execute(Long id) {
        TariffV2 tariff = repository.get(id);
        tariff.deactivate();
        return repository.save(tariff);
    }
}
