package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.mapper;


import com.github.jenkaby.bikerental.equipment.domain.model.Equipment;
import com.github.jenkaby.bikerental.equipment.domain.model.EquipmentStatus;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.repository.EquipmentStatusJpaRepository;
import com.github.jenkaby.bikerental.shared.exception.ReferenceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


public abstract class EquipmentJpaMapperDecorator implements EquipmentJpaMapper {

    @Autowired
    @Qualifier("delegate")
    private EquipmentJpaMapper delegate;
    @Autowired
    private EquipmentStatusJpaRepository statusRepository;
    @Autowired
    private EquipmentStatusJpaMapper statusMapper;

    @Override
    public Equipment toDomain(EquipmentJpaEntity entity) {
        Equipment domain = delegate.toDomain(entity);
        domain.setStatus(statusMapper.toDomain(statusRepository.findBySlug(entity.getStatusSlug())
                .orElseThrow(() -> new ReferenceNotFoundException(EquipmentStatus.class, entity.getStatusSlug()))));
        return domain;
    }
}
