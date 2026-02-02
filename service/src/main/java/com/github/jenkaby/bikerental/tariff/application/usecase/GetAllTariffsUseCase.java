package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;

public interface GetAllTariffsUseCase {
    Page<Tariff> execute(PageRequest pageRequest);
}
