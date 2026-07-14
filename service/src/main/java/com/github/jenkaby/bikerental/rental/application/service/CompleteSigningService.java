package com.github.jenkaby.bikerental.rental.application.service;

import com.github.jenkaby.bikerental.rental.RentalSigningVersionMismatchException;
import com.github.jenkaby.bikerental.rental.application.mapper.RentalEventMapper;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import com.github.jenkaby.bikerental.shared.infrastructure.port.clock.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompleteSigningService {

    private static final String RENTAL_EVENTS_EXCHANGER = "rental-events";

    private final RentalRepository rentalRepository;
    private final EventPublisher eventPublisher;
    private final RentalEventMapper eventMapper;
    private final TimeProvider timeProvider;

    @Transactional
    public void completeSigning(Long rentalId, Long expectedVersion, Instant signedAt) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, rentalId.toString()));

        if (!rental.getVersion().equals(expectedVersion)) {
            throw new RentalSigningVersionMismatchException(rentalId, expectedVersion, rental.getVersion());
        }

        LocalDateTime startedAt = LocalDateTime.ofInstant(signedAt, timeProvider.zoneId());
        rental.completeSigning(startedAt);

        Rental saved = rentalRepository.save(rental);
        eventPublisher.publish(RENTAL_EVENTS_EXCHANGER, eventMapper.toRentalStarted(saved));
        log.info("Rental {} signing completed at {}", saved.getId(), startedAt);
    }
}
