package com.github.jenkaby.bikerental.rental.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
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
            throw new IllegalStateException("Cannot select customer for rental that is not in DRAFT status");
        }
        this.customerId = customerId;
        this.updatedAt = Instant.now();
    }

    public void selectEquipment(Long equipmentId) {
        if (this.status != RentalStatus.DRAFT) {
            throw new IllegalStateException("Cannot select equipment for rental that is not in DRAFT status");
        }
        this.equipmentId = equipmentId;
        this.updatedAt = Instant.now();
    }

    public void selectTariff(Long tariffId) {
        if (this.status != RentalStatus.DRAFT) {
            throw new IllegalStateException("Cannot select tariff for rental that is not in DRAFT status");
        }
        this.tariffId = tariffId;
        this.updatedAt = Instant.now();
    }

    public void setPlannedDuration(Duration duration, LocalDateTime startTime) {
        this.plannedDuration = duration;
        this.startedAt = startTime;
        this.expectedReturnAt = startTime.plus(duration);
        this.updatedAt = Instant.now();
    }

    public void setEstimatedCost(Money estimatedCost) {
        this.estimatedCost = estimatedCost;
        this.updatedAt = Instant.now();
    }

    public boolean canBeActivated() {
        return status == RentalStatus.DRAFT
                && customerId != null
                && equipmentId != null
                && tariffId != null
                && plannedDuration != null
                && estimatedCost != null;
    }
}
