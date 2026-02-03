package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.tariff.application.usecase.DeactivateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DeactivateTariffService implements DeactivateTariffUseCase {

    private final TariffRepository repository;

    DeactivateTariffService(TariffRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Tariff execute(Long id) {
        Tariff tariff = repository.get(id);

        tariff.deactivate();

        return repository.save(tariff);
    }
}
