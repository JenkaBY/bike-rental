package com.github.jenkaby.bikerental.equipment.infrastructure.persistence.specification;

import com.github.jenkaby.bikerental.equipment.infrastructure.persistence.entity.EquipmentJpaEntity;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

@And({
        @Spec(path = EquipmentSpecConstant.STATUS_SLUG, params = EquipmentSpecConstant.STATUS, spec = Equal.class),
        @Spec(path = EquipmentSpecConstant.TYPE_SLUG, params = EquipmentSpecConstant.TYPE, spec = Equal.class)
})
@Or({
        @Spec(path = EquipmentSpecConstant.UID, params = EquipmentSpecConstant.SEARCH, spec = LikeIgnoreCase.class),
        @Spec(path = EquipmentSpecConstant.SERIAL_NUMBER, params = EquipmentSpecConstant.SEARCH, spec = LikeIgnoreCase.class),
        @Spec(path = EquipmentSpecConstant.MODEL, params = EquipmentSpecConstant.SEARCH, spec = LikeIgnoreCase.class)
})
public interface EquipmentSpec extends Specification<EquipmentJpaEntity> {
}
