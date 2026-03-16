package com.github.jenkaby.bikerental.finance.domain.model;

public enum PaymentType {
    PREPAYMENT,
    ADDITIONAL_PAYMENT,
    //    must be negative
    REFUND,
    CHANGE
}
