package com.github.jenkaby.bikerental.rental.application.usecase;

import com.github.jenkaby.bikerental.equipment.EquipmentSearchFilter;
import com.github.jenkaby.bikerental.rental.domain.model.AvailableForRentalEquipment;
import com.github.jenkaby.bikerental.shared.domain.model.vo.Page;
import com.github.jenkaby.bikerental.shared.domain.model.vo.PageRequest;

public interface GetAvailableForRentEquipmentsUseCase {

    Page<AvailableForRentalEquipment> getAvailableEquipments(EquipmentSearchFilter filter, PageRequest pageRequest);
}