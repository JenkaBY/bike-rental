package com.github.jenkaby.bikerental.rental.domain.model;

import com.github.jenkaby.bikerental.rental.domain.model.vo.RentalEquipmentCostBreakdown;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.*;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalEquipment {

    private Long id;
    private Long equipmentId;
    private String equipmentUid;
    private String equipmentType;

    private RentalEquipmentStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime expectedReturnAt;
    private LocalDateTime actualReturnAt;

    private Long tariffId;
    private Money estimatedCost;
    private Money finalCost;
    @Nullable
    private RentalEquipmentCostBreakdown finalCostBreakdown;

    public RentalEquipment activateForRental(Rental rental) {
        if (rental.getStartedAt() == null) {
            throw new IllegalArgumentException("Rental must have a start date to activate equipment");
        }
        if (rental.getExpectedReturnAt() == null) {
            throw new IllegalArgumentException("Rental must have a Expected return at to activate equipment");
        }
        this.setStatus(RentalEquipmentStatus.ACTIVE);
        this.setStartedAt(rental.getStartedAt());
        this.setExpectedReturnAt(rental.getExpectedReturnAt());
        return this;
    }

    public RentalEquipment markReturned(LocalDateTime returnAt) {
        this.setStatus(RentalEquipmentStatus.RETURNED);
        this.setActualReturnAt(returnAt);
        return this;
    }

    public void applyEstimatedCost(Long tariffId, Money estimatedCost) {
        this.tariffId = tariffId;
        this.estimatedCost = estimatedCost;
    }

    public void applyFinalCost(Long tariffId, Money finalCost, @Nullable RentalEquipmentCostBreakdown breakdown) {
        this.tariffId = tariffId;
        this.finalCost = finalCost;
        this.finalCostBreakdown = breakdown;
    }

    public static RentalEquipment assigned(Long equipmentId, String equipmentUid, String equipmentType) {
        return RentalEquipment.builder()
                .equipmentId(equipmentId)
                .equipmentUid(equipmentUid)
                .status(RentalEquipmentStatus.ASSIGNED)
                .equipmentType(equipmentType)
                .build();
    }
}

