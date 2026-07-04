package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.agreement.web.command.dto.SignAgreementRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class SignAgreementRequestTransformer {

    public static final String VALID_SIGNATURE_PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==";

    @DataTableType
    public SignAgreementRequest signAgreementRequest(Map<String, String> entry) {
        return new SignAgreementRequest(
                VALID_SIGNATURE_PNG_BASE64,
                DataTableHelper.toLong(entry, "rentalVersion"),
                DataTableHelper.toLong(entry, "templateId"),
                DataTableHelper.getStringOrNull(entry, "operatorId"));
    }
}
