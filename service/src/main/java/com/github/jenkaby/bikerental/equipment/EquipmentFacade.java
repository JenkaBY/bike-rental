package com.github.jenkaby.bikerental.equipment;

import java.util.Optional;


public interface EquipmentFacade {

    Optional<EquipmentInfo> findById(Long equipmentId);
}
