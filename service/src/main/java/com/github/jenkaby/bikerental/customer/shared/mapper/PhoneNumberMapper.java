package com.github.jenkaby.bikerental.customer.shared.mapper;

import com.github.jenkaby.bikerental.customer.domain.model.vo.PhoneNumber;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PhoneNumberMapper {

    default PhoneNumber toPhoneNumber(String phone) {
        return phone == null ? null : new PhoneNumber(phone);
    }

    default String toString(PhoneNumber phoneNumber) {
        return phoneNumber != null ? phoneNumber.value() : null;
    }
}
