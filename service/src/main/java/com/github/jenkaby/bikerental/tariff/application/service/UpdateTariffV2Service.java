package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.tariff.application.mapper.TariffV2CommandToDomainMapper;
import com.github.jenkaby.bikerental.tariff.application.usecase.UpdateTariffV2UseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffV2Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UpdateTariffV2Service implements UpdateTariffV2UseCase {

    private final TariffV2Repository repository;
    private final TariffV2CommandToDomainMapper mapper;

    UpdateTariffV2Service(TariffV2Repository repository, TariffV2CommandToDomainMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public TariffV2 execute(UpdateTariffV2Command command) {
        repository.get(command.id());
        TariffV2 tariff = mapper.toTariffV2(command);
        return repository.save(tariff);
    }
}
