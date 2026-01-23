package com.github.jenkaby.bikerental.customer.web.command.mapper;

import com.github.jenkaby.bikerental.customer.application.usecase.CreateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.model.vo.EmailAddress;
import com.github.jenkaby.bikerental.customer.domain.model.vo.PhoneNumber;
import com.github.jenkaby.bikerental.customer.web.command.dto.CreateCustomerRequest;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerCommandMapper {

    CreateCustomerUseCase.CreateCustomerCommand toCreateCommand(CreateCustomerRequest request);

    CustomerResponse toResponse(Customer customer);

    default String map(PhoneNumber phone) {
        return phone != null ? phone.value() : null;
    }

    default String map(EmailAddress email) {
        return email != null ? email.value() : null;
    }
}
