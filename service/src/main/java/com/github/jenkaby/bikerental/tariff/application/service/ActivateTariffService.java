package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.tariff.application.usecase.ActivateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ActivateTariffService implements ActivateTariffUseCase {

    private final TariffRepository repository;

    ActivateTariffService(TariffRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Tariff execute(Long id) {
        Tariff tariff = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Tariff.class, id.toString()));

        tariff.activate();

        return repository.save(tariff);
    }
}
