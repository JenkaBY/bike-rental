package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
public class RentalOverdueCalculator {

    private final Clock clock;

    public RentalOverdueCalculator(Clock clock) {
        this.clock = clock;
    }

    public Integer calculateOverdueMinutes(Rental rental) {
        if (rental.getStatus() == RentalStatus.ACTIVE && rental.getExpectedReturnAt() != null) {
            LocalDateTime now = LocalDateTime.now(clock);
            log.info("Now is {}. ReturnAt {}", now, rental.getExpectedReturnAt());
            if (rental.getExpectedReturnAt().isBefore(now)) {
                Duration overdueTime = Duration.between(rental.getExpectedReturnAt(), now);
                return (int) overdueTime.toMinutes();
            }
        }
        return 0;
    }
}
