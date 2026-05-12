package com.github.jenkaby.bikerental.rental.domain.repository;

import java.util.Set;

public interface RentalEquipmentRepository {

    Set<Long> findOccupiedEquipmentIds(Set<Long> candidateIds);
}