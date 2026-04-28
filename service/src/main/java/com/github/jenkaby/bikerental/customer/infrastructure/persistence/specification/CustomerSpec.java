package com.github.jenkaby.bikerental.customer.infrastructure.persistence.specification;

import com.github.jenkaby.bikerental.customer.infrastructure.persistence.entity.CustomerJpaEntity;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;

@And({
        @Spec(path = SpecConstant.PHONE, params = SpecConstant.PHONE, spec = Like.class)
})
public interface CustomerSpec extends Specification<CustomerJpaEntity> {
}