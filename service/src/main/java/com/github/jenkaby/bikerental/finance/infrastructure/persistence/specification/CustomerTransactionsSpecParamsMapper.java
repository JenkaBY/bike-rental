package com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification;

import com.github.jenkaby.bikerental.finance.domain.model.TransactionHistoryFilter;
import com.github.jenkaby.bikerental.shared.infrastructure.persistence.BusinessDayBoundaryResolver;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mapper(injectionStrategy = InjectionStrategy.SETTER)
public abstract class CustomerTransactionsSpecParamsMapper {

    private static final String PARAM_FROM_DATE = "fromDate";
    private static final String PARAM_TO_DATE = "toDate";

    private BusinessDayBoundaryResolver boundaryResolver;

    @Autowired
    public void setBoundaryResolver(BusinessDayBoundaryResolver boundaryResolver) {
        this.boundaryResolver = boundaryResolver;
    }

    public Map<String, String> toParams(TransactionHistoryFilter filter) {
        var result = new HashMap<String, String>();
        result.put(PARAM_FROM_DATE, Optional.ofNullable(filter.fromDate()).map(boundaryResolver::startOfDay).orElse(null));
        result.put(PARAM_TO_DATE, Optional.ofNullable(filter.toDate()).map(boundaryResolver::startOfNextDay).orElse(null));
        result.put(SpecConstant.TransactionField.SOURCE_ID, filter.sourceId());
        result.put(SpecConstant.TransactionField.SOURCE_TYPE, filter.sourceType() != null ? filter.sourceType().name() : null);
        return result;
    }
}
