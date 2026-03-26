# Task 003: Modify CreateCustomerService — Publish CustomerRegistered Event

> **Applied Skill:** `spring-boot-modulith/SKILL.md` — Event publishing after aggregate save; use the shared
> `EventPublisher` port (not Spring's `ApplicationEventPublisher` directly); `Propagation.MANDATORY` on the
> publisher enforces the transaction boundary.
> `spring-boot-data-ddd/SKILL.md` — Call `repository.save()` before publishing events to ensure the aggregate row
> is flushed before the listener reads it.

## 1. Objective

Inject `EventPublisher` into `CreateCustomerService` and publish a `CustomerRegistered` event after the customer
is successfully saved. The event carries the saved customer's `id` (UUID). This is the only change to the customer
module required by FR-FIN-02.

## 2. File to Modify

* **File Path:**
  `service/src/main/java/com/github/jenkaby/bikerental/customer/application/service/CreateCustomerService.java`
* **Action:** Modify Existing File

## 3. Code Implementation

**Imports Required (add to the existing import block):**

```java
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.event.CustomerRegistered;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
```

**Replace the entire class with the following:**

* **Location:** Full class replacement — do NOT alter the package declaration or existing imports; only the class
  body changes as shown below.

```java
package com.github.jenkaby.bikerental.customer.application.service;

import com.github.jenkaby.bikerental.customer.application.mapper.CustomerCommandToDomainMapper;
import com.github.jenkaby.bikerental.customer.application.usecase.CreateCustomerUseCase;
import com.github.jenkaby.bikerental.customer.domain.exception.DuplicatePhoneException;
import com.github.jenkaby.bikerental.customer.domain.model.Customer;
import com.github.jenkaby.bikerental.customer.domain.repository.CustomerRepository;
import com.github.jenkaby.bikerental.customer.shared.mapper.PhoneNumberMapper;
import com.github.jenkaby.bikerental.shared.domain.CustomerRef;
import com.github.jenkaby.bikerental.shared.domain.event.CustomerRegistered;
import com.github.jenkaby.bikerental.shared.infrastructure.messaging.EventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateCustomerService implements CreateCustomerUseCase {

    static final String CUSTOMER_EVENTS_DESTINATION = "customer-events";

    private final CustomerRepository repository;
    private final CustomerCommandToDomainMapper mapper;
    private final PhoneNumberMapper phoneMapper;
    private final EventPublisher eventPublisher;

    CreateCustomerService(
            CustomerRepository repository,
            CustomerCommandToDomainMapper mapper,
            PhoneNumberMapper phoneMapper,
            EventPublisher eventPublisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.phoneMapper = phoneMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Customer execute(CreateCustomerCommand command) {
        var phoneNumber = phoneMapper.toPhoneNumber(command.phone());

        if (repository.existsByPhone(phoneNumber.value())) {
            throw new DuplicatePhoneException(Customer.class.getSimpleName(), phoneNumber.value());
        }

        Customer customer = mapper.toCustomer(command);
        Customer saved = repository.save(customer);

        eventPublisher.publish(CUSTOMER_EVENTS_DESTINATION, new CustomerRegistered(CustomerRef.of(saved.getId())));

        return saved;
    }
}
```

**Change summary:**

1. Added `EventPublisher eventPublisher` field and constructor parameter.
2. Stored `repository.save(customer)` result in `saved` (was previously discarded as return value).
3. Called `eventPublisher.publish(...)` after saving — guarantees the customer row is persisted before the
   `BEFORE_COMMIT` listener fires.
4. Added constant `CUSTOMER_EVENTS_DESTINATION = "customer-events"` for the destination label used in logging.
5. Wrapped `saved.getId()` in `CustomerRef.of(...)` — the event field is now a typed `CustomerRef` value object
   rather than a raw `UUID`, consistent with how `Account` models customer identity in the finance module.

## 4. Validation Steps

```bash
./gradlew :service:compileJava
```
