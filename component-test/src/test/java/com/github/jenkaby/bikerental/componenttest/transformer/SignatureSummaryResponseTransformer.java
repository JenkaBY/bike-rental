package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.agreement.web.query.dto.SignatureSummaryResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class SignatureSummaryResponseTransformer {

    @DataTableType
    public SignatureSummaryResponse signatureSummaryResponse(Map<String, String> entry) {
        return new SignatureSummaryResponse(
                DataTableHelper.toLong(entry, "signatureId"),
                DataTableHelper.toLong(entry, "templateId"),
                DataTableHelper.toInt(entry, "templateVersionNumber"),
                null);
    }
}
