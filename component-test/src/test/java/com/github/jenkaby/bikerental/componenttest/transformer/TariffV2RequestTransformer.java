package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.componenttest.model.TariffV2RequestBuilder;
import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class TariffV2RequestTransformer {

    @DataTableType
    public TariffV2RequestBuilder transform(Map<String, String> entry) {
        var name = DataTableHelper.getStringOrNull(entry, "name");
        var description = DataTableHelper.getStringOrNull(entry, "description");
        var equipmentTypeSlug = DataTableHelper.getStringOrNull(entry, "equipmentType");
        var pricingType = PricingType.valueOf(entry.get("pricingType").trim());
        var validFrom = DataTableHelper.toLocalDate(entry, "validFrom");
        var validTo = DataTableHelper.toLocalDate(entry, "validTo");

        return new TariffV2RequestBuilder(
                name,
                description,
                equipmentTypeSlug,
                pricingType,
                validFrom,
                validTo
        );
    }
}
