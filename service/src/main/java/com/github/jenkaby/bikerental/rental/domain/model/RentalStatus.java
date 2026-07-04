package com.github.jenkaby.bikerental.rental.domain.model;

import com.github.jenkaby.bikerental.rental.domain.exception.InvalidRentalStatusException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public enum RentalStatus {
    DRAFT,
    AWAITING_SIGNATURE,
    ACTIVE,
    COMPLETED,
    CANCELLED,
    DEBT;

    private static final Map<RentalStatus, Set<RentalStatus>> ALLOWED_TRANSITIONS;

    static {
        var map = new EnumMap<RentalStatus, Set<RentalStatus>>(RentalStatus.class);
        map.put(DRAFT, Set.of(ACTIVE, CANCELLED, AWAITING_SIGNATURE));
        map.put(AWAITING_SIGNATURE, Set.of(ACTIVE, DRAFT, CANCELLED));
        map.put(ACTIVE, Set.of(CANCELLED, COMPLETED, DEBT));
        map.put(COMPLETED, Set.of());
        map.put(CANCELLED, Set.of());
        map.put(DEBT, Set.of(COMPLETED));
        ALLOWED_TRANSITIONS = Map.copyOf(map);
    }

    public void validateTransitionTo(RentalStatus target) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target)) {
            throw new InvalidRentalStatusException(this, target);
        }
    }
}
