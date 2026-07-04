package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.agreement.domain.model.AgreementTemplateStatus;
import com.github.jenkaby.bikerental.agreement.infrastructure.persistence.entity.AgreementTemplateJpaEntity;
import com.github.jenkaby.bikerental.componenttest.transformer.shared.Aliases;
import io.cucumber.java.DataTableType;

import java.util.Map;
import java.util.Optional;

public class AgreementTemplateJpaEntityTransformer {

    @DataTableType
    public AgreementTemplateJpaEntity transform(Map<String, String> entry) {
        var statusString = DataTableHelper.getStringOrNull(entry, "status");
        var status = statusString != null ? AgreementTemplateStatus.valueOf(statusString) : null;
        var contentSha256 = Optional.ofNullable(DataTableHelper.getStringOrNull(entry, "contentSha256"))
                .map(Aliases::getValueOrDefault)
                .orElse(null);
        return AgreementTemplateJpaEntity.builder()
                .id(DataTableHelper.toLong(entry, "id"))
                .lockVersion(0L)
                .versionNumber(DataTableHelper.toInt(entry, "versionNumber"))
                .title(DataTableHelper.getStringOrNull(entry, "title"))
                .content(DataTableHelper.getStringOrNull(entry, "content"))
                .contentSha256(contentSha256)
                .status(status)
                .createdAt(DataTableHelper.parseLocalDateTimeToInstant(entry, "createdAt"))
                .updatedAt(DataTableHelper.parseLocalDateTimeToInstant(entry, "updatedAt"))
                .activatedAt(DataTableHelper.parseLocalDateTimeToInstant(entry, "activatedAt"))
                .deactivatedAt(DataTableHelper.parseLocalDateTimeToInstant(entry, "deactivatedAt"))
                .build();
    }
}
