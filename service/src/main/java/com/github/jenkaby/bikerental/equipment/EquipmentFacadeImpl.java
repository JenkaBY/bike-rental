package com.github.jenkaby.bikerental.equipment;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentToInfoMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByIdUseCase;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class EquipmentFacadeImpl implements EquipmentFacade {

    private final GetEquipmentByIdUseCase getEquipmentByIdUseCase;
    private final EquipmentToInfoMapper equipmentToInfoMapper;

    EquipmentFacadeImpl(
            GetEquipmentByIdUseCase getEquipmentByIdUseCase,
            EquipmentToInfoMapper equipmentToInfoMapper) {
        this.getEquipmentByIdUseCase = getEquipmentByIdUseCase;
        this.equipmentToInfoMapper = equipmentToInfoMapper;
    }

    @Override
    public Optional<EquipmentInfo> findById(Long equipmentId) {
        return getEquipmentByIdUseCase.execute(equipmentId)
                .map(equipmentToInfoMapper::toEquipmentInfo);
    }
}
