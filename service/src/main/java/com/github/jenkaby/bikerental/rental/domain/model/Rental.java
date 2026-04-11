package com.github.jenkaby.bikerental.rental.domain.model;

import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.exception.RentalNotReadyForActivationException;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationResult;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Rental {

    private static final Predicate<RentalEquipment> RETURNED = e -> e.getStatus() == RentalEquipmentStatus.RETURNED;
    @Setter
    private Long id;

    private UUID customerId;
    private List<RentalEquipment> equipments;

    @Setter
    private RentalStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime expectedReturnAt;
    private LocalDateTime actualReturnAt;

    private Duration plannedDuration;
    private Duration actualDuration;

    private Money estimatedCost;
    private Money finalCost;

    private Instant createdAt;
    private Instant updatedAt;


    public static Rental createDraft() {
        return Rental.builder()
                .status(RentalStatus.DRAFT)
                .createdAt(Instant.now())
                .equipments(new ArrayList<>())
                .build();
    }

    public void selectCustomer(UUID customerId) {
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }
        this.customerId = customerId;
        this.updatedAt = Instant.now();
    }

    public void selectTariff(Long tariffId) {
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }
        this.updatedAt = Instant.now();
    }

    public void setPlannedDuration(Duration duration) {
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }
        this.plannedDuration = duration;
        // startedAt and expectedReturnAt will be set automatically when rental is activated
        this.updatedAt = Instant.now();
    }

    public void setEstimatedCost(Money estimatedCost) {
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }
        this.estimatedCost = estimatedCost;
        this.updatedAt = Instant.now();
    }

    public Money getEstimatedCost() {
        return this.equipments.stream()
                .map(RentalEquipment::getEstimatedCost)
                .reduce(Money.zero(), Money::add);
    }

    public RentalRef toRentalRef() {
        return new RentalRef(id);
    }

    public boolean isPrepaymentSufficient(Money amount) {
        if (estimatedCost == null) {
            return false;
        }
        return amount.compareTo(estimatedCost) >= 0;
    }

    public boolean hasActiveStatus() {
        return status == RentalStatus.ACTIVE;
    }

    public boolean canBeActivated() {
        boolean hasEquipment = !isEmpty(equipments);
        return status == RentalStatus.DRAFT
                && customerId != null
                && hasEquipment
                && plannedDuration != null
                && estimatedCost != null;
    }

    public void activate(LocalDateTime actualStartTime) {
        // Validate status
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }

        // Validate required fields
        if (!canBeActivated()) {
            List<String> missingFields = new ArrayList<>();
            if (customerId == null) missingFields.add("customerId");
            if (plannedDuration == null) missingFields.add("plannedDuration");
            if (estimatedCost == null) missingFields.add("estimatedCost");
            if (equipments == null) missingFields.add("equipmentIds");
            throw new RentalNotReadyForActivationException(missingFields);
        }

        this.status = RentalStatus.ACTIVE;
        this.startedAt = actualStartTime; // Actual start time
        this.expectedReturnAt = actualStartTime.plus(this.plannedDuration);

        equipments.forEach(e -> e.activateForRental(this));
        this.updatedAt = Instant.now();
    }

    // For cases update rental during patch
    public void clearEquipmentRentals() {
        this.equipments.clear();
    }

    public void addEquipment(RentalEquipment equipment) {
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }
        this.equipments.add(equipment);
        this.updatedAt = Instant.now();
    }

    public boolean allEquipmentReturned() {
        return equipments.stream()
                .allMatch(RETURNED);
    }

    private List<RentalEquipment> rentedEquipments() {
        return equipments.stream()
                .filter(Predicate.not(RETURNED))
                .toList();
    }

    public List<RentalEquipment> equipmentsToReturn(List<Long> toReturnEquipmentIds, List<String> toReturnEquipmentUids, LocalDateTime returnedAt) {
//         assume when no equipments are present in request, entire rental must be completed
        var isEmptyRequest = isEmpty(toReturnEquipmentIds) && isEmpty(toReturnEquipmentUids);
        Predicate<RentalEquipment> filter = eq -> isEmptyRequest
                || toReturnEquipmentIds.contains(eq.getEquipmentId())
                || toReturnEquipmentUids.contains(eq.getEquipmentUid());
        return rentedEquipments().stream()
                .filter(filter)
                .map(eq -> eq.markReturned(returnedAt))
                .toList();
    }

    public RentalDurationResult calculateActualDuration(RentalDurationCalculator calculator, LocalDateTime returnTime) {
        if (this.status != RentalStatus.ACTIVE && this.status != RentalStatus.COMPLETED) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.ACTIVE);
        }

        if (this.startedAt == null) {
            throw new IllegalStateException("Cannot calculate duration: rental start time is not set");
        }

        RentalDurationResult result = calculator.calculate(this.startedAt, returnTime);
        this.actualDuration = result.actualDuration();
        this.actualReturnAt = returnTime;
        this.updatedAt = Instant.now();

        return result;
    }

    public void completeForDebt() {
        if (this.finalCost == null) {
            throw new IllegalArgumentException("Final cost cannot be null");
        }
        this.updatedAt = Instant.now();
        this.status = RentalStatus.COMPLETED;
    }

    public void completeWithStatus(Money finalCost, RentalStatus status) {
        // Validate status
        validateCompletion(finalCost);

        this.finalCost = finalCost;
        if (allEquipmentReturned()) {
            this.status = status;
        }
        this.updatedAt = Instant.now();
    }

    private void validateCompletion(Money finalCost) {
        if (this.status != RentalStatus.ACTIVE && this.status != RentalStatus.DEBT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.ACTIVE);
        }

        if (this.startedAt == null) {
            throw new IllegalStateException("Cannot complete rental: rental start time is not set");
        }

        if (finalCost == null) {
            throw new IllegalArgumentException("Final cost cannot be null");
        }

        if (this.actualReturnAt == null || this.actualDuration == null) {
            throw new IllegalStateException("Cannot complete rental: actual return time and duration must be set before completing");
        }
    }

    private static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
