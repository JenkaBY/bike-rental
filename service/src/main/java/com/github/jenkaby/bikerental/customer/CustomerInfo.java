package com.github.jenkaby.bikerental.customer;

import java.time.LocalDate;
import java.util.UUID;

public record CustomerInfo(
        UUID id,
        String phone,
        String firstName,
        String lastName,
        String email,
        LocalDate birthDate
) {
}
