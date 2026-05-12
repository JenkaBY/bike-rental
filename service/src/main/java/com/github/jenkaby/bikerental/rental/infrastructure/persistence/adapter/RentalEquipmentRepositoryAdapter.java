package com.github.jenkaby.bikerental.rental.infrastructure.persistence.adapter;

import com.github.jenkaby.bikerental.rental.domain.model.RentalEquipmentStatus;
import com.github.jenkaby.bikerental.rental.domain.repository.RentalEquipmentRepository;
import com.github.jenkaby.bikerental.rental.infrastructure.persistence.repository.RentalEquipmentJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
class RentalEquipmentRepositoryAdapter implements RentalEquipmentRepository {

    private static final Set<String> OCCUPIED_STATUSES = RentalEquipmentStatus.occupiedStatuses().stream()
            .map(RentalEquipmentStatus::name)
            .collect(Collectors.toSet());

    private final RentalEquipmentJpaRepository repository;

    RentalEquipmentRepositoryAdapter(RentalEquipmentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Set<Long> findOccupiedEquipmentIds(Set<Long> candidateIds) {
        if (candidateIds.isEmpty()) {
            return Set.of();
        }
        return repository.findEquipmentIdsByEquipmentIdInAndStatusIn(candidateIds, OCCUPIED_STATUSES);
    }
}