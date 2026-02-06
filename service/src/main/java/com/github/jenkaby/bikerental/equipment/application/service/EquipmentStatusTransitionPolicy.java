package com.github.jenkaby.bikerental.equipment.application.service;

import com.github.jenkaby.bikerental.equipment.domain.exception.InvalidStatusTransitionException;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.domain.repository.EquipmentStatusRepository;
import com.github.jenkaby.bikerental.equipment.domain.service.StatusTransitionPolicy;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;


@Service
public class EquipmentStatusTransitionPolicy implements StatusTransitionPolicy {

    private final EquipmentStatusRepository statusRepository;

    public EquipmentStatusTransitionPolicy(EquipmentStatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    @Override
    public void validateTransition(@NonNull String fromStatusSlug, @NonNull String toStatusSlug) {
        validateTransition(null, fromStatusSlug, toStatusSlug);
    }

    public void validateTransition(@Nullable Object equipmentId, @NonNull String fromStatusSlug, @NonNull String toStatusSlug) {
        if (!statusRepository.existsBySlug(toStatusSlug)) {
            throw new ReferenceNotFoundException(EquipmentStatus.class, toStatusSlug);
        }

        EquipmentStatus fromStatus = statusRepository.findBySlug(fromStatusSlug)
                .orElseThrow(() -> new ReferenceNotFoundException(EquipmentStatus.class, fromStatusSlug));

        if (!fromStatus.canTransitionTo(toStatusSlug)) {
            throw new InvalidStatusTransitionException(equipmentId, fromStatusSlug, toStatusSlug);
        }
    }
}
