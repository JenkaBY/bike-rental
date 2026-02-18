package com.github.jenkaby.bikerental.rental.domain.model;

import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;
import com.github.jenkaby.bikerental.rental.domain.exception.RentalNotReadyForActivationException;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationCalculator;
import com.github.jenkaby.bikerental.rental.domain.service.RentalDurationResult;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Rental {

    @Setter
    private Long id;
    
    private UUID customerId;
    private Long equipmentId;
    private Long tariffId;
    
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
                .build();
    }

    public void selectCustomer(UUID customerId) {
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }
        this.customerId = customerId;
        this.updatedAt = Instant.now();
    }

    public void selectEquipment(Long equipmentId) {
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }
        this.equipmentId = equipmentId;
        this.updatedAt = Instant.now();
    }

    public void selectTariff(Long tariffId) {
        if (this.status != RentalStatus.DRAFT) {
            throw new InvalidRentalStatusException(this.status, RentalStatus.DRAFT);
        }
        this.tariffId = tariffId;
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


    public boolean isPrepaymentSufficient(Money amount) {
        if (estimatedCost == null) {
            return false;
        }
        return amount.compareTo(estimatedCost) >= 0;
    }

    public boolean canBeActivated() {
        return status == RentalStatus.DRAFT
                && customerId != null
                && equipmentId != null
                && tariffId != null
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
            if (equipmentId == null) missingFields.add("equipmentId");
            if (tariffId == null) missingFields.add("tariffId");
            if (plannedDuration == null) missingFields.add("plannedDuration");
            if (estimatedCost == null) missingFields.add("estimatedCost");
            throw new RentalNotReadyForActivationException(missingFields);
        }

        this.status = RentalStatus.ACTIVE;
        this.startedAt = actualStartTime; // Actual start time
        this.expectedReturnAt = actualStartTime.plus(this.plannedDuration);
        this.updatedAt = Instant.now();
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
}
