package com.github.jenkaby.bikerental.componenttest.transformer;

import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2Status;
import com.github.jenkaby.bikerental.tariff.shared.utils.TariffV2FieldNames;
import com.github.jenkaby.bikerental.tariff.web.query.dto.PricingParams;
import com.github.jenkaby.bikerental.tariff.web.query.dto.TariffV2Response;
import io.cucumber.java.DataTableType;

import java.util.Map;

public class TariffV2ResponseTransformer {

    @DataTableType
    public TariffV2Response tariffV2Response(Map<String, String> entry) {
        var id = DataTableHelper.toLong(entry, "id");
        var name = DataTableHelper.getStringOrNull(entry, "name");
        var description = DataTableHelper.getStringOrNull(entry, "description");
        var equipmentTypeSlug = DataTableHelper.getStringOrNull(entry, "equipmentType");
        if (equipmentTypeSlug == null) {
            equipmentTypeSlug = DataTableHelper.getStringOrNull(entry, "equipmentType");
        }
        var pricingType = entry.get("pricingType") != null && !entry.get("pricingType").isBlank()
                ? PricingType.valueOf(entry.get("pricingType").trim())
                : null;
        var params = new PricingParams(
                DataTableHelper.toBigDecimal(entry, TariffV2FieldNames.FIRST_HOUR_PRICE),
                DataTableHelper.toBigDecimal(entry, TariffV2FieldNames.HOURLY_DISCOUNT),
                DataTableHelper.toBigDecimal(entry, TariffV2FieldNames.MINIMUM_HOURLY_PRICE),
                DataTableHelper.toBigDecimal(entry, TariffV2FieldNames.HOURLY_PRICE),
                DataTableHelper.toBigDecimal(entry, TariffV2FieldNames.DAILY_PRICE),
                DataTableHelper.toBigDecimal(entry, TariffV2FieldNames.OVERTIME_HOURLY_PRICE),
                DataTableHelper.toBigDecimal(entry, TariffV2FieldNames.ISSUANCE_FEE),
                DataTableHelper.toInt(entry, TariffV2FieldNames.MINIMUM_DURATION_MINUTES),
                DataTableHelper.toBigDecimal(entry, TariffV2FieldNames.MINIMUM_DURATION_SURCHARGE),
                DataTableHelper.toBigDecimal(entry, TariffV2FieldNames.PRICE)
        );
        var validFrom = DataTableHelper.toLocalDate(entry, "validFrom");
        var validTo = DataTableHelper.toLocalDate(entry, "validTo");
        var version = DataTableHelper.getStringOrNull(entry, "version");
        var status = entry.get("status") != null && !entry.get("status").isBlank()
                ? TariffV2Status.valueOf(entry.get("status").trim())
                : null;

        return new TariffV2Response(
                id,
                name,
                description,
                equipmentTypeSlug,
                pricingType,
                params,
                validFrom,
                validTo,
                version,
                status
        );
    }
}
