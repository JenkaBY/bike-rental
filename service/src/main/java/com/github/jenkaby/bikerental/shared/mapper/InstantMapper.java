package com.github.jenkaby.bikerental.shared.mapper;

import com.github.jenkaby.bikerental.shared.infrastructure.port.clock.TimeProvider;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;

@Mapper
public abstract class InstantMapper {

    private TimeProvider timeProvider;

    @Autowired
    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    public LocalDateTime instantToLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, timeProvider.zoneId()) : null;
    }

    public Instant localDateTimeToInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atZone(timeProvider.zoneId()).toInstant() : null;
    }
}
