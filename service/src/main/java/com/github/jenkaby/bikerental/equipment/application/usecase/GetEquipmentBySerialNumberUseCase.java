package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.SerialNumber;

import java.util.Optional;

public interface GetEquipmentBySerialNumberUseCase {
    Optional<Equipment> execute(SerialNumber serialNumber);
}
