package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;

import java.util.List;

public interface GetEquipmentByIdsUseCase {

    List<Equipment> execute(List<Long> id);
}
