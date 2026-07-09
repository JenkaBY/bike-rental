package com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification;

import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mapper(injectionStrategy = InjectionStrategy.SETTER)
public abstract class CustomerTransactionsSpecParamsMapper {

    private static final String PARAM_FROM_DATE = "fromDate";
    private static final String PARAM_TO_DATE = "toDate";

    private ZoneId businessZoneId;

    @Autowired
    public void setBusinessZoneId(ZoneId businessZoneId) {
        this.businessZoneId = businessZoneId;
    }

    public Map<String, String> toParams(TransactionHistoryFilter filter) {
        var result = new HashMap<String, String>();
        result.put(PARAM_FROM_DATE, Optional.ofNullable(filter.fromDate()).map(this::toStartOfDay).map(this::formatInstant).orElse(null));
        result.put(PARAM_TO_DATE, Optional.ofNullable(filter.toDate()).map(this::toStartOfNextDay).map(this::formatInstant).orElse(null));
        result.put(SpecConstant.TransactionField.SOURCE_ID, filter.sourceId());
        result.put(SpecConstant.TransactionField.SOURCE_TYPE, filter.sourceType() != null ? filter.sourceType().name() : null);
        return result;
    }

    private Instant toStartOfDay(LocalDate date) {
        return date.atStartOfDay(businessZoneId).toInstant();
    }

    private Instant toStartOfNextDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay(businessZoneId).toInstant();
    }

    private String formatInstant(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }
}
