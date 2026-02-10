package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffPeriod;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffSelectionResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class TariffSelectionResponseTransformer {

    @DataTableType
    public TariffSelectionResponse transform(Map<String, String> entry) {
        var id = DataTableHelper.toLong(entry, "id");
        var name = DataTableHelper.getStringOrNull(entry, "name");
        var equipmentType = DataTableHelper.getStringOrNull(entry, "equipmentType");
        var price = DataTableHelper.toBigDecimal(entry, "price");
        var period = TariffPeriod.valueOf(entry.get("period"));

        return new TariffSelectionResponse(
                id,
                name,
                equipmentType,
                price,
                period
        );
    }
}
