package com.github.jenkaby.bikerental.rental.application.service.validator;

import com.github.jenkaby.bikerental.equipment.EquipmentInfo;

import java.util.List;

public interface RequestedEquipmentValidator {

    void validateAvailability(List<EquipmentInfo> equipments);

    void validateSize(List<Long> requestedEquipmentIds, List<EquipmentInfo> equipments);
}
