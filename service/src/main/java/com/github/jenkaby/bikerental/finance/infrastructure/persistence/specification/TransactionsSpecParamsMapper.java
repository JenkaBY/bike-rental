package com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification;

import com.github.jenkaby.bikerental.finance.domain.model.TransactionFilter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class TransactionsSpecParamsMapper {

    private static final String PARAM_FROM_DATE = "fromDate";
    private static final String PARAM_TO_DATE = "toDate";

    private final BusinessDayBoundaryResolver boundaryResolver;

    TransactionsSpecParamsMapper(BusinessDayBoundaryResolver boundaryResolver) {
        this.boundaryResolver = boundaryResolver;
    }

    public Map<String, String> toParams(TransactionFilter filter) {
        var result = new HashMap<String, String>();
        result.put(PARAM_FROM_DATE, Optional.ofNullable(filter.fromDate()).map(boundaryResolver::startOfDay).orElse(null));
        result.put(PARAM_TO_DATE, Optional.ofNullable(filter.toDate()).map(boundaryResolver::startOfNextDay).orElse(null));
        result.put(SpecConstant.TransactionField.SOURCE_ID, filter.sourceId());
        result.put(SpecConstant.TransactionField.SOURCE_TYPE, filter.sourceType() != null ? filter.sourceType().name() : null);
        return result;
    }
}
