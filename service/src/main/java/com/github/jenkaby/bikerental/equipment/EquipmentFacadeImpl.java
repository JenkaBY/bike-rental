package com.github.jenkaby.bikerental.equipment;

import com.github.jenkaby.bikerental.equipment.application.mapper.EquipmentToInfoMapper;
import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByIdUseCase;
import com.github.jenkaby.bikerental.equipment.application.usecase.GetEquipmentByIdsUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
class EquipmentFacadeImpl implements EquipmentFacade {

    private final GetEquipmentByIdUseCase getEquipmentByIdUseCase;
    private final GetEquipmentByIdsUseCase getEquipmentByIdsUseCase;
    private final EquipmentToInfoMapper equipmentToInfoMapper;

    EquipmentFacadeImpl(GetEquipmentByIdUseCase getEquipmentByIdUseCase,
                        GetEquipmentByIdsUseCase getEquipmentByIdsUseCase,
            EquipmentToInfoMapper equipmentToInfoMapper) {
        this.getEquipmentByIdUseCase = getEquipmentByIdUseCase;
        this.getEquipmentByIdsUseCase = getEquipmentByIdsUseCase;
        this.equipmentToInfoMapper = equipmentToInfoMapper;
    }

    @Override
    public Optional<EquipmentInfo> findById(Long equipmentId) {
        return getEquipmentByIdUseCase.execute(equipmentId)
                .map(equipmentToInfoMapper::toEquipmentInfo);
    }

    @Override
    public List<EquipmentInfo> findByIds(List<Long> equipmentIds) {
        return getEquipmentByIdsUseCase.execute(equipmentIds)
                .stream()
                .map(equipmentToInfoMapper::toEquipmentInfo)
                .toList();
    }
}
