package com.github.jenkaby.bikerental.shared.infrastructure.persistence;

import com.github.jenkaby.bikerental.shared.infrastructure.port.clock.TimeProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class BusinessDayBoundaryResolver {

    private final TimeProvider timeProvider;

    public BusinessDayBoundaryResolver(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public String startOfDay(LocalDate date) {
        return format(date.atStartOfDay(timeProvider.zoneId()).toInstant());
    }

    public String startOfNextDay(LocalDate date) {
        return format(date.plusDays(1).atStartOfDay(timeProvider.zoneId()).toInstant());
    }

    private String format(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}
