package com.github.jenkaby.bikerental.finance.web.command.dto;

import java.util.UUID;

public record RecordPaymentResponse(UUID paymentId, String receiptNumber) {
}
