package com.github.jenkaby.bikerental.customer.application.mapper;

import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.model.vo.EmailAddress;
import com.github.jenkaby.bikerental.customer.domain.model.vo.PhoneNumber;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper {

    CustomerInfo toInfo(Customer customer);

    default String map(PhoneNumber phone) {
        return phone != null ? phone.value() : null;
    }

    default String map(EmailAddress email) {
        return email != null ? email.value() : null;
    }
}
