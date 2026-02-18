package com.github.jenkaby.bikerental.rental.domain.service;

import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;

public interface RentalDurationCalculator {

    RentalDurationResult calculate(@NonNull LocalDateTime start, @NonNull LocalDateTime end);
}
