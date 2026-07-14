package com.github.jenkaby.bikerental.shared.infrastructure.port.clock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
@Component
public class DefaultTimeProvider implements TimeProvider {

    private final Clock clock;
    private final ZoneId businessTZ;

    @Override
    public LocalDateTime nowTruncated() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS);
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(clock).truncatedTo(ChronoUnit.MILLIS);
    }

    @Override
    public Instant nowTruncatedInstant() {
        return clock.instant().truncatedTo(ChronoUnit.MILLIS);
    }

    @Override
    public Instant nowInstant() {
        return clock.instant().truncatedTo(ChronoUnit.MILLIS);
    }

    @Override
    public LocalDate today() {
        return LocalDate.now(clock);
    }

    @Override
    public ZoneId zoneId() {
        return businessTZ;
    }

    @Override
    public ZoneOffset zoneOffset() {
        return businessTZ.getRules().getOffset(clock.instant());
    }
}
