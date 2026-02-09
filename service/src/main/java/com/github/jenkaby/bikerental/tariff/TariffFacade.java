package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;


public interface TariffFacade {

    Optional<TariffInfo> findById(Long tariffId);

    TariffInfo selectTariff(String equipmentTypeSlug, Duration duration, LocalDate rentalDate);

    Money calculateEstimatedCost(Long tariffId, Duration duration);
}
