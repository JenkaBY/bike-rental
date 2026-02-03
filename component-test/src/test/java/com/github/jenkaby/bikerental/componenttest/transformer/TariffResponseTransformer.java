package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.tariff.domain.model.TariffStatus;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffResponse;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class TariffResponseTransformer {

    @DataTableType
    public TariffResponse transform(Map<String, String> entry) {
        var id = DataTableHelper.toLong(entry, "id");
        var name = DataTableHelper.getStringOrNull(entry, "name");
        var description = DataTableHelper.getStringOrNull(entry, "description");
        var equipmentTypeSlug = DataTableHelper.getStringOrNull(entry, "equipmentType");
        var basePrice = DataTableHelper.toBigDecimal(entry, "basePrice");
        var halfHourPrice = DataTableHelper.toBigDecimal(entry, "halfHourPrice");
        var hourPrice = DataTableHelper.toBigDecimal(entry, "hourPrice");
        var dayPrice = DataTableHelper.toBigDecimal(entry, "dayPrice");
        var hourDiscountedPrice = DataTableHelper.toBigDecimal(entry, "discountedPrice");

        var validFrom = DataTableHelper.toLocalDate(entry, "validFrom");
        var validTo = DataTableHelper.toLocalDate(entry, "validTo");

        var status = TariffStatus.valueOf(entry.get("status"));

        return new TariffResponse(
                id,
                name,
                description,
                equipmentTypeSlug,
                basePrice,
                halfHourPrice,
                hourPrice,
                dayPrice,
                hourDiscountedPrice,
                validFrom,
                validTo,
                status
        );
    }
}
