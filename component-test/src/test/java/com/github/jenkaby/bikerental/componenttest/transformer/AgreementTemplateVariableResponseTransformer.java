package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateVariableResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class AgreementTemplateVariableResponseTransformer {

    @DataTableType
    public AgreementTemplateVariableResponse agreementTemplateVariableResponse(Map<String, String> entry) {
        return new AgreementTemplateVariableResponse(
                DataTableHelper.getStringOrNull(entry, "key"),
                DataTableHelper.getStringOrNull(entry, "description"),
                DataTableHelper.getStringOrNull(entry, "example"));
    }
}
