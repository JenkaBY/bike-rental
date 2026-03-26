package com.github.jenkaby.bikerental.finance.infrastructure.eventlistener;

import com.github.jenkaby.bikerental.finance.application.usecase.CreateCustomerAccountUseCase;
import com.github.jenkaby.bikerental.shared.domain.event.CustomerRegistered;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
class FinanceCustomerEventListener {

    private final CreateCustomerAccountUseCase createCustomerAccountUseCase;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onCustomerRegistered(CustomerRegistered event) {
        log.info("Received CustomerRegistered event for customerId={}", event.customerRef().id());
        createCustomerAccountUseCase.execute(event.customerRef().id());
    }
}
