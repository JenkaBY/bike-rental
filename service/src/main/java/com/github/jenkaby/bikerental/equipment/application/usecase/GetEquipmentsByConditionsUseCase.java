package com.github.jenkaby.bikerental.equipment.application.usecase;

import com.github.jenkaby.bikerental.equipment.EquipmentSearchFilter;
import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.shared.domain.model.Condition;

import java.util.List;
import java.util.Set;

public interface GetEquipmentsByConditionsUseCase {

    List<Equipment> execute(Set<Condition> conditions, EquipmentSearchFilter filter);
}
