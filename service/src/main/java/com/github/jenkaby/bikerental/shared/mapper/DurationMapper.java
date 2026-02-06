package com.github.jenkaby.bikerental.shared.mapper;

import org.mapstruct.Mapper;

import java.time.Duration;


@Mapper
public interface DurationMapper {


    default Duration toDuration(Integer minutes) {
        return minutes != null ? Duration.ofMinutes(minutes) : null;
    }

    default Integer toMinutes(Duration duration) {
        return duration != null ? (int) duration.toMinutes() : null;
    }
}
