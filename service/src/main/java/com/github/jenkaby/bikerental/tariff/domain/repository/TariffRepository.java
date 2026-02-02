package com.github.jenkaby.bikerental.tariff.domain.repository;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;

import java.util.List;
import java.util.Optional;

public interface TariffRepository {

    Tariff save(Tariff tariff);

    Optional<Tariff> findById(Long id);

    Page<Tariff> findAll(PageRequest pageRequest);

    List<Tariff> findActiveForEquipmentType(String equipmentTypeSlug);

    Tariff get(Long id);
}
