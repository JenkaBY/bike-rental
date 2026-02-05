package com.github.jenkaby.bikerental.equipment.domain.model;

import com.github.jenkaby.bikerental.equipment.domain.exception.InvalidStatusTransitionException;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Equipment {

    private Long id;
    private SerialNumber serialNumber;
    private Uid uid;
    private String typeSlug;
    private EquipmentStatus status;
    private String model;
    private LocalDate commissionedAt;
    private String condition;

    public void changeStatusTo(EquipmentStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(this.id, this.status, newStatus);
        }
        this.status = newStatus;
    }

    public void setInitialStatus(EquipmentStatus initialStatus) {
        this.status = initialStatus;
    }
}
