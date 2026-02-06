package com.github.jenkaby.bikerental.shared.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.time.Duration;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DurationMapper {


    default Duration toDuration(Integer minutes) {
        return minutes != null ? Duration.ofMinutes(minutes) : null;
    }

    default Integer toMinutes(Duration duration) {
        return duration != null ? (int) duration.toMinutes() : null;
    }
}
