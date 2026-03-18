package com.github.jenkaby.bikerental.tariff.domain.repository;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TariffV2Repository {

    TariffV2 save(TariffV2 tariff);

    Optional<TariffV2> findById(Long id);

    Page<TariffV2> findAll(PageRequest pageRequest);

    List<TariffV2> findActiveForEquipmentType(String equipmentTypeSlug);

    List<TariffV2> findActiveByEquipmentTypeAndValidOn(String equipmentTypeSlug, LocalDate date);

    TariffV2 get(Long id);
}
