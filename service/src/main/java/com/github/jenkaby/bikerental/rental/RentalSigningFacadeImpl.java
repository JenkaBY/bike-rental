package com.github.jenkaby.bikerental.rental;

import com.github.jenkaby.bikerental.rental.application.service.CompleteSigningService;
import com.github.jenkaby.bikerental.rental.domain.model.Rental;
import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipment;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalRepository;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import com.github.jenkaby.bikerental.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
class RentalSigningFacadeImpl implements RentalSigningFacade {

    private final RentalRepository rentalRepository;
    private final CompleteSigningService completeSigningService;

    RentalSigningFacadeImpl(RentalRepository rentalRepository, CompleteSigningService completeSigningService) {
        this.rentalRepository = rentalRepository;
        this.completeSigningService = completeSigningService;
    }

    @Override
    public RentalSigningSnapshot getSigningSnapshot(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException(Rental.class, rentalId.toString()));

        if (rental.getStatus() != RentalStatus.AWAITING_SIGNATURE) {
            throw new RentalNotAwaitingSignatureException(rentalId, rental.getStatus());
        }

        List<RentalSigningSnapshot.EquipmentItem> equipments = rental.getEquipments().stream()
                .map(this::toEquipmentItem)
                .toList();

        return new RentalSigningSnapshot(
                rental.getId(),
                rental.getVersion(),
                rental.getCustomerId(),
                rental.getPlannedDuration(),
                amountOf(rental.getEstimatedCost()),
                equipments);
    }

    @Override
    public void completeSigning(Long rentalId, Long expectedVersion, Instant signedAt) {
        completeSigningService.completeSigning(rentalId, expectedVersion, signedAt);
    }

    private RentalSigningSnapshot.EquipmentItem toEquipmentItem(RentalEquipment equipment) {
        return new RentalSigningSnapshot.EquipmentItem(
                equipment.getEquipmentId(),
                equipment.getEquipmentUid(),
                equipment.getEquipmentType(),
                amountOf(equipment.getEstimatedCost()));
    }

    private BigDecimal amountOf(Money money) {
        return money == null ? null : money.amount();
    }
}
