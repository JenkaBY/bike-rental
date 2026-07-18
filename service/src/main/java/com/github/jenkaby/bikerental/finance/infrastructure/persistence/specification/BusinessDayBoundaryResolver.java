package com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class BusinessDayBoundaryResolver {

    private final ZoneId businessZoneId;

//FIXME inject zoneId from TimeProvider
    BusinessDayBoundaryResolver(ZoneId businessZoneId) {
        this.businessZoneId = businessZoneId;
    }
// TODO refactor @RentalSpecParamsMapper to use this new component instead of duplicating the logic
    public String startOfDay(LocalDate date) {
        return format(date.atStartOfDay(businessZoneId).toInstant());
    }

    public String startOfNextDay(LocalDate date) {
        return format(date.plusDays(1).atStartOfDay(businessZoneId).toInstant());
    }

    private String format(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}
