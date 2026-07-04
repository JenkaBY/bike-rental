package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.agreement.web.command.dto.AgreementPdfPreviewRequest;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class AgreementPdfPreviewRequestTransformer {

    @DataTableType
    public AgreementPdfPreviewRequest agreementPdfPreviewRequest(Map<String, String> entry) {
        var content = Optional.ofNullable(DataTableHelper.getStringOrNull(entry, "content"))
                .map(text -> text.replace("\\n", "\n"))
                .orElse(null);
        return new AgreementPdfPreviewRequest(
                DataTableHelper.getStringOrNull(entry, "title"),
                content);
    }
}
