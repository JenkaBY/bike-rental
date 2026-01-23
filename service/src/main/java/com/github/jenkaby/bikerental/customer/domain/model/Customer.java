package com.github.jenkaby.bikerental.customer.domain.model;

import com.github.jenkaby.bikerental.customer.domain.model.vo.EmailAddress;
import com.github.jenkaby.bikerental.customer.domain.model.vo.PhoneNumber;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Customer {
    @Setter
    private UUID id;
    private final PhoneNumber phone;
    private final String firstName;
    private final String lastName;
    private final EmailAddress email;
    private final LocalDate birthDate;
}
