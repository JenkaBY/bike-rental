package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementTemplateRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class AgreementTemplateRequestTransformer {

    @DataTableType
    public AgreementTemplateRequest agreementTemplateRequest(Map<String, String> entry) {
        return new AgreementTemplateRequest(
                DataTableHelper.getStringOrNull(entry, "title"),
                DataTableHelper.getStringOrNull(entry, "content"));
    }
}
