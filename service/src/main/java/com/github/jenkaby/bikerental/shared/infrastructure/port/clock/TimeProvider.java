package com.github.jenkaby.bikerental.shared.infrastructure.port.clock;

import java.time.*;

public interface TimeProvider {

    LocalDateTime nowTruncated();
    LocalDateTime now();
    Instant nowTruncatedInstant();
    Instant nowInstant();
    LocalDate today();
    ZoneId zoneId();
    ZoneOffset zoneOffset();
}
