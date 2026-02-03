package com.github.jenkaby.bikerental.tariff.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Tariff {
    @Setter
    private Long id;
    private final String name;
    private final String description;
    private final String equipmentTypeSlug;
    private final Money basePrice;
    private final Money halfHourPrice;
    private final Money hourPrice;
    private final Money dayPrice;
    private final Money hourDiscountedPrice;
    private final LocalDate validFrom;
    private final LocalDate validTo;
    private TariffStatus status;

    public boolean isActive() {
        return TariffStatus.ACTIVE == status;
    }

    public void activate() {
        this.status = TariffStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = TariffStatus.INACTIVE;
    }

    public boolean isValidOn(LocalDate date) {
        boolean afterStart = !date.isBefore(validFrom);
        boolean beforeEnd = validTo == null || !date.isAfter(validTo);
        return afterStart && beforeEnd;
    }
}
