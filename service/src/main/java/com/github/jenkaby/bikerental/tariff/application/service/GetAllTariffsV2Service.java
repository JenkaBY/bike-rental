package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetAllTariffsV2UseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import com.github.jenkaby.bikerental.tariff.domain.repository.TariffV2Repository;
import org.springframework.stereotype.Service;

@Service
class GetAllTariffsV2Service implements GetAllTariffsV2UseCase {

    private final TariffV2Repository repository;

    GetAllTariffsV2Service(TariffV2Repository repository) {
        this.repository = repository;
    }

    @Override
    public Page<TariffV2> execute(PageRequest pageRequest) {
        return repository.findAll(pageRequest);
    }
}
