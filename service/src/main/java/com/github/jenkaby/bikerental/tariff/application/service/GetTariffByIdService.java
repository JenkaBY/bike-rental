package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffRepository;
import org.springframework.stereotype.Service;

@Service
class GetTariffByIdService implements GetTariffByIdUseCase {

    private final TariffRepository repository;

    GetTariffByIdService(TariffRepository repository) {
        this.repository = repository;
    }

    @Override
    public Tariff execute(Long id) {
        return repository.get(id);
    }
}
