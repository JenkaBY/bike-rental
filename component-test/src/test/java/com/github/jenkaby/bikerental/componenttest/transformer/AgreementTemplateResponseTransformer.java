package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;
import com.github.jenkaby.bikerental.agreement.web.query.dto.AgreementTemplateResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class AgreementTemplateResponseTransformer {

    @DataTableType
    public AgreementTemplateResponse agreementTemplateResponse(Map<String, String> entry) {
        var status = Optional.ofNullable(DataTableHelper.getStringOrNull(entry, "status"))
                .map(AgreementTemplateStatus::valueOf)
                .orElse(null);
        return new AgreementTemplateResponse(
                DataTableHelper.toLong(entry, "id"),
                DataTableHelper.toInt(entry, "versionNumber"),
                DataTableHelper.getStringOrNull(entry, "title"),
                DataTableHelper.getStringOrNull(entry, "content"),
                status,
                DataTableHelper.parseLocalDateTimeToInstant(entry, "createdAt"),
                DataTableHelper.parseLocalDateTimeToInstant(entry, "activatedAt"),
                DataTableHelper.parseLocalDateTimeToInstant(entry, "deactivatedAt"));
    }
}
