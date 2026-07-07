package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.agreement.web.query.dto.RentalAgreementResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class RentalAgreementResponseTransformer {

    @DataTableType
    public RentalAgreementResponse rentalAgreementResponse(Map<String, String> entry) {
        return new RentalAgreementResponse(
                DataTableHelper.toLong(entry, "templateId"),
                DataTableHelper.toInt(entry, "versionNumber"),
                DataTableHelper.getStringOrNull(entry, "title"),
                DataTableHelper.getStringOrNull(entry, "content"));
    }
}
