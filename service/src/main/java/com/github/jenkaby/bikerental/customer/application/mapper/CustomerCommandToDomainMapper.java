package com.github.jenkaby.bikerental.customer.application.mapper;

import com.github.jenkaby.bikerental.customer.application.usecase.CreateCustomerUseCase.CreateCustomerCommand;
import com.github.jenkaby.bikerental.customer.application.usecase.UpdateCustomerUseCase.UpdateCustomerCommand;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.shared.mapper.EmailAddressMapper;
import com.github.jenkaby.bikerental.customer.shared.mapper.PhoneNumberMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PhoneNumberMapper.class, EmailAddressMapper.class})
public interface CustomerCommandToDomainMapper {

    @Mapping(target = "id", ignore = true)
    Customer toCustomer(CreateCustomerCommand command);

    @Mapping(target = "id", source = "customerId")
    Customer toCustomer(UpdateCustomerCommand command);
}
