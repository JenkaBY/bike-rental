package com.github.jenkaby.bikerental.shared.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InstantMapper {


    default LocalDateTime instantToLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }

    default Instant localDateTimeToInstant(LocalDateTime localDateTime) {
        LocalDateTime localDateTime = localDateTime.
        return localDateTime != null ? localDateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
    }
}
