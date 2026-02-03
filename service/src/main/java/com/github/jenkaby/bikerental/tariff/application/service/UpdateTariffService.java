package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.tariff.application.mapper.TariffCommandToDomainMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.UpdateTariffUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateTariffService implements UpdateTariffUseCase {

    private final TariffRepository repository;
    private final TariffCommandToDomainMapper mapper;

    UpdateTariffService(TariffRepository repository, TariffCommandToDomainMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Tariff execute(UpdateTariffCommand command) {
        repository.get(command.id());

        Tariff tariff = mapper.toTariff(command);
        return repository.save(tariff);
    }
}
