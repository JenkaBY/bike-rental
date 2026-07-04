package com.github.jenkaby.bikerental.rental;

import com.github.jenkaby.bikerental.rental.application.service.CompleteSigningService;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
class RentalSigningFacadeImpl implements RentalSigningFacade {

    private final RentalRepository rentalRepository;
    private final CompleteSigningService completeSigningService;
    private final RentalSigningSnapshotMapper snapshotMapper;

    RentalSigningFacadeImpl(RentalRepository rentalRepository,
                            CompleteSigningService completeSigningService,
                            RentalSigningSnapshotMapper snapshotMapper) {
        this.rentalRepository = rentalRepository;
        this.completeSigningService = completeSigningService;
        this.snapshotMapper = snapshotMapper;
    }

    @Override
    public RentalSigningSnapshot getSigningSnapshot(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, rentalId.toString()));

        if (rental.getStatus() != RentalStatus.AWAITING_SIGNATURE) {
            throw new RentalNotAwaitingSignatureException(rentalId, rental.getStatus());
        }

        return snapshotMapper.toSnapshot(rental);
    }

    @Override
    public void completeSigning(Long rentalId, Long expectedVersion, Instant signedAt) {
        completeSigningService.completeSigning(rentalId, expectedVersion, signedAt);
    }
}
