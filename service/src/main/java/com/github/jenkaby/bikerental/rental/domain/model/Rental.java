package com.github.jenkaby.bikerental.rental.domain.model;

import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.exception.RentalNotReadyForActivationException;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationResult;
import com.github.jenkaby.bikerental.shared.domain.RentalRef;
import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


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

    private Long specialTariffId;
    private Money specialPrice;
    private DiscountPercent discountPercent;

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

    public void setPlannedDuration(Duration duration) {
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }
        this.plannedDuration = duration;
        // startedAt and expectedReturnAt will be set automatically when rental is activated
        this.updatedAt = Instant.now();
    }

    public Money getEstimatedCost() {
        return calculateCost(RentalEquipment::getEstimatedCost);
    }

    public Money getFinalCost() {
        return calculateCost(RentalEquipment::getFinalCost);
    }

    private Money calculateCost(Function<RentalEquipment, Money> costExtractor) {
        if (specialPrice != null) {
            return specialPrice;
        }
        var subtotal = this.equipments.stream()
                .map(costExtractor)
                .filter(java.util.Objects::nonNull)
                .reduce(Money.zero(), Money::add);
        if (discountPercent != null) {
            return subtotal.subtract(discountPercent.multiply(subtotal));
        }
        return subtotal;
    }

    public RentalRef toRentalRef() {
        return new RentalRef(id);
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
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }

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

    public List<Long> getNewEquipmentIds(Set<Long> incomingIds) {
        var existingIds = equipments.stream()
                .map(RentalEquipment::getEquipmentId)
                .collect(Collectors.toSet());
        return incomingIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();
    }

    public void replaceEquipments(List<RentalEquipment> toAdd, Set<Long> incomingIds) {
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }
        equipments.removeIf(e -> !incomingIds.contains(e.getEquipmentId()));
        toAdd.forEach(this::addEquipment);
        this.updatedAt = Instant.now();
    }

    public void addEquipment(RentalEquipment equipment) {
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }
        this.equipments.add(equipment);
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status.validateTransitionTo(RentalStatus.CANCELLED);
        this.equipments.forEach(e -> e.setStatus(RentalEquipmentStatus.RETURNED));
        this.status = RentalStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public boolean allEquipmentsReturned() {
        return equipments.stream()
                .allMatch(RETURNED);
    }

    private List<RentalEquipment> rentedEquipments() {
        return equipments.stream()
                .filter(Predicate.not(RETURNED))
                .toList();
    }

    public List<RentalEquipment> equipmentsToReturn(List<Long> toReturnEquipmentIds, List<String> toReturnEquipmentUids, LocalDateTime returnedAt) {
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
        this.status = RentalStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void completeWithStatus(Money finalCost, RentalStatus status) {
        validateCompletion(finalCost);

        this.finalCost = finalCost;
        if (allEquipmentsReturned()) {
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

    public static class RentalBuilder {
        public Rental build() {
            if ((specialTariffId != null || specialPrice != null) && discountPercent != null) {
                throw new IllegalArgumentException(
                        "specialTariffId and discountPercent are mutually exclusive");
            }
            if ((specialTariffId != null && specialPrice == null)
                    || (specialTariffId == null && specialPrice != null)) {
                throw new IllegalArgumentException(
                        "specialPrice is required when specialTariffId is set");
            }
            return new Rental(id, customerId, equipments, status, startedAt, expectedReturnAt,
                    actualReturnAt, plannedDuration, actualDuration, estimatedCost, finalCost,
                    specialTariffId, specialPrice, discountPercent, createdAt, updatedAt);
        }
    }
}
