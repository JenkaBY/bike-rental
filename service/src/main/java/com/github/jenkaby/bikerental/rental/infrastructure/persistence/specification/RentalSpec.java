package com.github.jenkaby.bikerental.rental.infrastructure.persistence.specification;

import com.github.jenkaby.bikerental.rental.infrastructure.persistence.entity.RentalJpaEntity;
import jakarta.persistence.criteria.JoinType;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.LessThan;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

@Join(path = "rentalEquipments", alias = "re", type = JoinType.LEFT)
@And({
        @Spec(path = SpecConstant.RentalField.STATUS, params = SpecConstant.RentalField.STATUS, spec = Equal.class),
        @Spec(path = SpecConstant.RentalField.CUSTOMER_ID, params = SpecConstant.RentalField.CUSTOMER_ID, spec = Equal.class),
        @Spec(path = SpecConstant.RentalField.EQUIPMENT_UID, params = SpecConstant.RentalField.PARAM_EQUIPMENT_UID, spec = Equal.class),
        @Spec(path = SpecConstant.RentalField.CREATED_AT, params = SpecConstant.RentalField.PARAM_CREATED_FROM, spec = GreaterThanOrEqual.class, config = SpecConstant.INSTANT_FORMAT),
        @Spec(path = SpecConstant.RentalField.CREATED_AT, params = SpecConstant.RentalField.PARAM_CREATED_TO, spec = LessThan.class, config = SpecConstant.INSTANT_FORMAT),
})
public interface RentalSpec extends Specification<RentalJpaEntity> {
}
