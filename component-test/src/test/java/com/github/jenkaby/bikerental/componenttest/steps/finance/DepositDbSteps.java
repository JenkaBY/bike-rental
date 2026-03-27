package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.FinanceScenarioContext;
import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.AccountJpaRepository;
import com.github.jenkaby.bikerental.finance.infrastructure.persistence.repository.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DepositDbSteps {

    private final ScenarioContext scenarioContext;
    private final FinanceScenarioContext financeContext;
    private final AccountJpaRepository accountJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;



//
//    @Then("the customer wallet balance is increased by {bigdecimal} in db")
//    public void theCustomerWalletBalanceIsIncreasedBy(BigDecimal expectedIncrease) {
//        String customerId = scenarioContext.extractFromLastCustomerResponse("$.id");
//        var account = accountJpaRepository.findByCustomerId(UUID.fromString(customerId))
//                .orElseThrow(() -> new AssertionError("Customer finance account not found for customerId=" + customerId));
//
//        var wallet = account.getSubLedgers().stream()
//                .filter(sl -> sl.getLedgerType() == LedgerType.CUSTOMER_WALLET)
//                .findFirst()
//                .orElseThrow(() -> new AssertionError("CUSTOMER_WALLET sub-ledger not found"));
//
//        assertThat(wallet.getBalance())
//                .as("CUSTOMER_WALLET balance should equal deposit amount")
//                .isEqualByComparingTo(expectedIncrease);
//    }
//
//    @Then("a transaction record exists in db with type {string} and paymentMethod {string} and operatorId {string}")
//    public void aTransactionRecordExistsInDb(String type, String paymentMethod, String operatorId) {
//        String transactionId = scenarioContext.retrieve("depositTransactionId", String.class);
//        TransactionJpaEntity tx = transactionJpaRepository.findById(UUID.fromString(transactionId))
//                .orElseThrow(() -> new AssertionError("Transaction not found in db for id=" + transactionId));
//
//        assertSoftly(softly -> {
//            softly.assertThat(tx.getTransactionType().name()).as("transaction type").isEqualTo(type);
//            softly.assertThat(tx.getPaymentMethod().name()).as("payment method").isEqualTo(paymentMethod);
//            softly.assertThat(tx.getOperatorId()).as("operatorId").isEqualTo(operatorId);
//            softly.assertThat(tx.getRecords()).as("must have exactly 2 transaction records").hasSize(2);
//        });
//    }
}
