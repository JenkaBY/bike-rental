package com.github.jenkaby.bikerental.rental.domain.service;

import java.time.Duration;


public record BaseRentalDurationResult(
        int billableMinutes,
        Duration actualDuration
) implements RentalDurationResult {
}
