package com.github.jenkaby.bikerental.componenttest.transformer.finance;

import com.github.jenkaby.bikerental.componenttest.transformer.DataTableHelper;
import com.github.jenkaby.bikerental.finance.web.query.dto.CustomerAccountBalancesResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class CustomerAccountBalancesResponseTransformer {

    @DataTableType
    public CustomerAccountBalancesResponse transform(Map<String, String> row) {
        return new CustomerAccountBalancesResponse(
                DataTableHelper.toBigDecimal(row, "walletBalance"),
                DataTableHelper.toBigDecimal(row, "holdBalance"),
                DataTableHelper.parseLocalDateTimeToInstant(row, "lastUpdated")
        );
    }
}
