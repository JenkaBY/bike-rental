package com.github.jenkaby.bikerental.rental.domain.model;

import java.util.Set;

public enum RentalEquipmentStatus {
    //    Assigned == reserved
    ASSIGNED,
    //    Rental started
    ACTIVE,
    RETURNED;

    public static Set<RentalEquipmentStatus> occupiedStatuses() {
        return Set.of(ASSIGNED, ACTIVE);
    }
}

