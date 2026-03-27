package com.github.jenkaby.bikerental.componenttest.steps.finance;

import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import com.github.jenkaby.bikerental.finance.web.command.dto.RecordDepositRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Slf4j
@RequiredArgsConstructor
public class DepositWebSteps {

    private final ScenarioContext scenarioContext;
    private final TestRestTemplate restClient;
    @LocalServerPort
    private final int port;
//
//    @When("staff records a cash deposit of {bigdecimal} for the customer with operator {string}")
//    public void staffRecordsCashDeposit(BigDecimal amount, String operatorId) {
//        String customerId = scenarioContext.extractFromLastResponse("$.id");
//        UUID idempotencyKey = UUID.randomUUID();
//        RecordDepositRequest request = new RecordDepositRequest(
//            idempotencyKey, UUID.fromString(customerId), amount, PaymentMethod.CASH, operatorId);
//        ResponseEntity<String> response = restClient.postForEntity(
//                "http://localhost:" + port + "/api/finance/deposits",
//                buildHttpEntity(request), String.class);
//        scenarioContext.setLastResponse(response);
//        log.info("Deposit response status: {}", response.getStatusCode());
//    }
//
//    @When("a deposit request is submitted for unknown customerId {string} with amount {bigdecimal} and paymentMethod {string} and operator {string}")
//    public void depositForUnknownCustomer(String customerId, BigDecimal amount, String paymentMethod, String operatorId) {
//        UUID idempotencyKey = UUID.randomUUID();
//        RecordDepositRequest request = new RecordDepositRequest(
//            idempotencyKey, UUID.fromString(customerId), amount, PaymentMethod.valueOf(paymentMethod), operatorId);
//        ResponseEntity<String> response = restClient.postForEntity(
//                "http://localhost:" + port + "/api/finance/deposits",
//                buildHttpEntity(request), String.class);
//        scenarioContext.setLastResponse(response);
//    }
//
//    @When("a deposit request is submitted for the customer with amount {bigdecimal} and paymentMethod {string} and operator {string}")
//    public void depositWithAmount(BigDecimal amount, String paymentMethod, String operatorId) {
//        String customerId = scenarioContext.extractFromLastResponse("$.id");
//        UUID idempotencyKey = UUID.randomUUID();
//        RecordDepositRequest request = new RecordDepositRequest(
//            idempotencyKey, UUID.fromString(customerId), amount, PaymentMethod.valueOf(paymentMethod), operatorId);
//        ResponseEntity<String> response = restClient.postForEntity(
//                "http://localhost:" + port + "/api/finance/deposits",
//                buildHttpEntity(request), String.class);
//        scenarioContext.setLastResponse(response);
//    }
//
//    @Then("the deposit response contains a transactionId")
//    public void theDepositResponseContainsATransactionId() {
//        String transactionId = scenarioContext.extractFromLastResponse("$.transactionId");
//        assertThat(transactionId).as("transactionId must be present in deposit response").isNotNull();
//        scenarioContext.store("depositTransactionId", transactionId);
//    }

    private HttpEntity<RecordDepositRequest> buildHttpEntity(RecordDepositRequest request) {
        HttpHeaders headers = new HttpHeaders();
        scenarioContext.getRequestHeaders().forEach((k, v) -> headers.put(k, v));
        headers.setContentType(MediaType.valueOf("application/vnd.bikerental.v1+json"));
        return new HttpEntity<>(request, headers);
    }
}
