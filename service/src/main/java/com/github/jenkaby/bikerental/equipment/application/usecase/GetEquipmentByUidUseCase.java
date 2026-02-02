package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.shared.domain.model.vo.Uid;

import java.util.Optional;

public interface GetEquipmentByUidUseCase {
    Optional<Equipment> execute(Uid uid);
}
