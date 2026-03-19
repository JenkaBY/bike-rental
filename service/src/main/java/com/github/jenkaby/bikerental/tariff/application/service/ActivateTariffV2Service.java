package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.tariff.application.usecase.ActivateTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffV2Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ActivateTariffV2Service implements ActivateTariffV2UseCase {

    private final TariffV2Repository repository;

    ActivateTariffV2Service(TariffV2Repository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public TariffV2 execute(Long id) {
        TariffV2 tariff = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TariffV2.class, id.toString()));
        tariff.activate();
        return repository.save(tariff);
    }
}
