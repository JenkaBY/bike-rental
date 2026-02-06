package com.github.jenkaby.bikerental.customer.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.infrastructure.persistence.entity.CustomerJpaEntity;
import com.github.jenkaby.bikerental.customer.shared.mapper.EmailAddressMapper;
import com.github.jenkaby.bikerental.customer.shared.mapper.PhoneNumberMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PhoneNumberMapper.class, EmailAddressMapper.class})
public interface CustomerJpaMapper {

    Customer toDomain(CustomerJpaEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    CustomerJpaEntity toEntity(Customer customer);
}
