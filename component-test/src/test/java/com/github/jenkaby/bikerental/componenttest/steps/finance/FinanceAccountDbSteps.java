package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.AccountJpaEntity;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.AccountJpaRepository;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Slf4j
@RequiredArgsConstructor
public class FinanceAccountDbSteps {

    private final ScenarioContext scenarioContext;
    private final AccountJpaRepository accountJpaRepository;

    @Then("a customer finance account is created in db for the registered customer")
    public void aCustomerFinanceAccountIsCreatedInDbForTheRegisteredCustomer() {
        String customerId = scenarioContext.getStringResponseBody()
                .contains("\"id\"") ? extractCustomerIdFromResponse() : null;

        assertThat(customerId).as("Customer ID in response must not be null").isNotNull();

        UUID customerUuid = UUID.fromString(customerId);

        AccountJpaEntity account = accountJpaRepository.findByCustomerId(customerUuid)
                .orElseThrow(() -> new AssertionError(
                        "Expected a Customer Finance Account for customerId=" + customerUuid + " but none was found"));

        assertSoftly(softly -> {
            softly.assertThat(account.getAccountType())
                    .as("Account type must be CUSTOMER")
                    .isEqualTo(com.github.jenkaby.bikerental.finance.domain.model.AccountType.CUSTOMER);
            softly.assertThat(account.getCustomerId())
                    .as("Account must reference the registered customer")
                    .isEqualTo(customerUuid);
            softly.assertThat(account.getId())
                    .as("Account id must not be null")
                    .isNotNull();
            softly.assertThat(account.getSubLedgers())
                    .as("Account must have exactly 2 sub-ledgers")
                    .hasSize(2);
        });

        var wallet = account.getSubLedgers().stream()
                .filter(sl -> sl.getLedgerType() == com.github.jenkaby.bikerental.finance.domain.model.LedgerType.CUSTOMER_WALLET)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected CUSTOMER_WALLET sub-ledger not found"));

        var hold = account.getSubLedgers().stream()
                .filter(sl -> sl.getLedgerType() == com.github.jenkaby.bikerental.finance.domain.model.LedgerType.CUSTOMER_HOLD)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected CUSTOMER_HOLD sub-ledger not found"));

        assertSoftly(softly -> {
            softly.assertThat(wallet.getBalance())
                    .as("CUSTOMER_WALLET balance must be zero")
                    .isEqualByComparingTo(BigDecimal.ZERO);
            softly.assertThat(hold.getBalance())
                    .as("CUSTOMER_HOLD balance must be zero")
                    .isEqualByComparingTo(BigDecimal.ZERO);
        });
    }

    private String extractCustomerIdFromResponse() {
        var body = scenarioContext.getStringResponseBody();
        int idIndex = body.indexOf("\"id\":\"");
        if (idIndex == -1) {
            return null;
        }
        int start = idIndex + 6;
        int end = body.indexOf("\"", start);
        return body.substring(start, end);
    }
}
