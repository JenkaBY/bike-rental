package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffByIdUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class GetTariffByIdService implements GetTariffByIdUseCase {

    private final TariffRepository repository;

    GetTariffByIdService(TariffRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Tariff> execute(Long id) {
        return repository.findById(id);
    }

    @Override
    public Tariff get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Tariff.class, id));
    }
}
