package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetTariffV2ByIdUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffV2Repository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class GetTariffV2ByIdService implements GetTariffV2ByIdUseCase {

    private final TariffV2Repository repository;

    GetTariffV2ByIdService(TariffV2Repository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<TariffV2> execute(Long id) {
        return repository.findById(id);
    }

    @Override
    public TariffV2 get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TariffV2.class, id.toString()));
    }
}
