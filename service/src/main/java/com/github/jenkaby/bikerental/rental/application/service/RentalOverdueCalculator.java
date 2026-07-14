package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.shared.infrastructure.port.clock.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class RentalOverdueCalculator {

    private final TimeProvider timeProvider;

//    TODO Create Overdue class for keeping overdue value
    public Integer calculateOverdueMinutes(Rental rental) {
        if (rental.getStatus() == RentalStatus.ACTIVE && rental.getExpectedReturnAt() != null) {
            LocalDateTime now = timeProvider.nowTruncated();
            if (rental.getExpectedReturnAt().isBefore(now)) {
                Duration overdueTime = Duration.between(rental.getExpectedReturnAt(), now);
                return (int) overdueTime.toMinutes();
            }
        }
        return 0;
    }
}
