package com.github.jenkaby.bikerental.customer.shared.mapper;

import com.github.jenkaby.bikerental.customer.domain.model.vo.EmailAddress;
import org.mapstruct.Mapper;

@Mapper
public interface EmailAddressMapper {

    default EmailAddress toEmailAddress(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return new EmailAddress(email);
    }

    default String toString(EmailAddress emailAddress) {
        return emailAddress != null ? emailAddress.value() : null;
    }
}
