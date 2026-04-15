package com.github.jenkaby.bikerental.rental.domain.service;

import java.time.Duration;


public interface RentalDurationResult {

    default int actualMinutes() {
        return (int) actualDuration().toMinutes();
    }

    int billableMinutes();

    Duration actualDuration();

    default Duration billableDuration() {
        return Duration.ofMinutes(billableMinutes());
    }
}
