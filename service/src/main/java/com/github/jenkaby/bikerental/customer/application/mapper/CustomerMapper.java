package com.github.jenkaby.bikerental.customer.application.mapper;

import com.github.jenkaby.bikerental.customer.CustomerInfo;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.shared.mapper.EmailAddressMapper;
import com.github.jenkaby.bikerental.customer.shared.mapper.PhoneNumberMapper;
import org.mapstruct.Mapper;

@Mapper(uses = {PhoneNumberMapper.class, EmailAddressMapper.class})
public interface CustomerMapper {

    CustomerInfo toInfo(Customer customer);
}
