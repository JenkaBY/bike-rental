package com.github.jenkaby.bikerental.customer.web.command.mapper;

import com.github.jenkaby.bikerental.customer.application.usecase.CreateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.application.usecase.UpdateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.shared.mapper.EmailAddressMapper;
import com.github.jenkaby.bikerental.customer.shared.mapper.PhoneNumberMapper;
import com.github.jenkaby.bikerental.customer.web.command.dto.CustomerRequest;
import com.github.jenkaby.bikerental.customer.web.query.dto.CustomerResponse;
import com.github.jenkaby.bikerental.shared.mapper.UuidMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(uses = {UuidMapper.class, PhoneNumberMapper.class, EmailAddressMapper.class})
public interface CustomerCommandMapper {

    CreateCustomerUseCase.CreateCustomerCommand toCreateCommand(CustomerRequest request);

    @Mapping(target = "customerId", source = "customerId")
    UpdateCustomerUseCase.UpdateCustomerCommand toUpdateCommand(UUID customerId, CustomerRequest request);

    CustomerResponse toResponse(Customer customer);
}
