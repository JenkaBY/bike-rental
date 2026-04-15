package com.github.jenkaby.bikerental.rental.domain.model;

import com.github.jenkaby.bikerental.shared.domain.model.vo.Money;
import lombok.*;

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

    public static RentalEquipment assigned(Long equipmentId, String equipmentUid, String equipmentType) {
        return RentalEquipment.builder()
                .equipmentId(equipmentId)
                .equipmentUid(equipmentUid)
                .status(RentalEquipmentStatus.ASSIGNED)
                .equipmentType(equipmentType)
                .build();
    }
}

