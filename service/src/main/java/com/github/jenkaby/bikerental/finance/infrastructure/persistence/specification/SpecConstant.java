package com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SpecConstant {

    public static final String INSTANT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public class TransactionField {
        public static final String CUSTOMER_ID = "customerId";
        public static final String RECORDED_AT = "recordedAt";
        public static final String SOURCE_ID = "sourceId";
        public static final String SOURCE_TYPE = "sourceType";
        public static final String LEDGER_TYPE = "ledgerType";
    }
}
