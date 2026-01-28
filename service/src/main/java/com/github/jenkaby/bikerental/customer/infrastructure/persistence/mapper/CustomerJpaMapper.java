package com.github.jenkaby.bikerental.customer.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.model.vo.EmailAddress;
import com.github.jenkaby.bikerental.customer.domain.model.vo.PhoneNumber;
import com.github.jenkaby.bikerental.customer.infrastructure.persistence.entity.CustomerJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerJpaMapper {

    Customer toDomain(CustomerJpaEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    CustomerJpaEntity toEntity(Customer customer);

    default String map(PhoneNumber phone) {
        return phone != null ? phone.value() : null;
    }

    default PhoneNumber mapPhone(String phone) {
        return phone != null ? new PhoneNumber(phone) : null;
    }

    default String map(EmailAddress email) {
        return email != null ? email.value() : null;
    }

    default EmailAddress mapEmail(String email) {
        return email != null ? new EmailAddress(email) : null;
    }
}
