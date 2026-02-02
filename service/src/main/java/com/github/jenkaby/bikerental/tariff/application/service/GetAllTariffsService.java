package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetAllTariffsUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffRepository;
import org.springframework.stereotype.Service;

@Service
class GetAllTariffsService implements GetAllTariffsUseCase {

    private final TariffRepository repository;

    GetAllTariffsService(TariffRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Tariff> execute(PageRequest pageRequest) {
        return repository.findAll(pageRequest);
    }
}
