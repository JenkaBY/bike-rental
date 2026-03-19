package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;

public interface GetAllTariffsV2UseCase {
    Page<TariffV2> execute(PageRequest pageRequest);
}
