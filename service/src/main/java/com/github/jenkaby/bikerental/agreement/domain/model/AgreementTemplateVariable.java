package com.github.jenkaby.bikerental.agreement.domain.model;

public enum AgreementTemplateVariable {

    CUSTOMER_FIRST_NAME("customer.firstName", "customer-first-name"),
    CUSTOMER_LAST_NAME("customer.lastName", "customer-last-name"),
    CUSTOMER_PHONE("customer.phone", "customer-phone"),
    RENTAL_STARTED_AT("rental.startedAt", "rental-started-at"),
    RENTAL_DURATION("rental.duration", "rental-duration"),
    RENTAL_TOTAL("rental.total", "rental-total"),
    RENTAL_NUMBER("rental.number", "rental-number");

    private final String key;
    private final String messageCodeFragment;

    AgreementTemplateVariable(String key, String messageCodeFragment) {
        this.key = key;
        this.messageCodeFragment = messageCodeFragment;
    }

    public String key() {
        return key;
    }

    public String placeholder() {
        return "{{" + key + "}}";
    }

    public String descriptionCode() {
        return "agreement.template.variable." + messageCodeFragment + ".description";
    }

    public String exampleCode() {
        return "agreement.template.variable." + messageCodeFragment + ".example";
    }
}
