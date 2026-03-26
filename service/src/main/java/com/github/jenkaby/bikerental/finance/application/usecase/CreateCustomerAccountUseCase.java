package com.github.jenkaby.bikerental.finance.application.usecase;

import java.util.UUID;

public interface CreateCustomerAccountUseCase {

    void execute(UUID customerId);
}
