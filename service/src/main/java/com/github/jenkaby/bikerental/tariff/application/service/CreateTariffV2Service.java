package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.tariff.application.mapper.TariffV2CommandToDomainMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.CreateTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffV2Repository;
import org.springframework.stereotype.Service;

@Service
class CreateTariffV2Service implements CreateTariffV2UseCase {

    private final TariffV2Repository repository;
    private final TariffV2CommandToDomainMapper mapper;

    CreateTariffV2Service(TariffV2Repository repository, TariffV2CommandToDomainMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public TariffV2 execute(CreateTariffV2Command command) {
        TariffV2 tariff = mapper.toTariffV2(command);
        return repository.save(tariff);
    }
}
