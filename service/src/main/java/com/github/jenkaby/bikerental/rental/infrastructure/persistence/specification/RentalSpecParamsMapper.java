package com.github.jenkaby.bikerental.rental.infrastructure.persistence.specification;

import com.github.jenkaby.bikerental.rental.domain.model.RentalSearchFilter;
import com.github.jenkaby.bikerental.rental.domain.model.RentalStatus;
import com.github.jenkaby.bikerental.shared.infrastructure.persistence.BusinessDayBoundaryResolver;
import org.jspecify.annotations.Nullable;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Mapper(injectionStrategy = InjectionStrategy.SETTER)
public abstract class RentalSpecParamsMapper {

    private BusinessDayBoundaryResolver boundaryResolver;

    @Autowired
    public void setBoundaryResolver(BusinessDayBoundaryResolver boundaryResolver) {
        this.boundaryResolver = boundaryResolver;
    }

    public Map<String, String[]> toParams(RentalSearchFilter filter) {
        var result = new HashMap<String, String[]>();
        if (!filter.statuses().isEmpty()) {
            result.put(SpecConstant.RentalField.STATUS, filter.statuses().stream().map(RentalStatus::name).toArray(String[]::new));
        }
        putIfPresent(result, SpecConstant.RentalField.CUSTOMER_ID, filter.customerId() != null ? filter.customerId().toString() : null);
        putIfPresent(result, SpecConstant.RentalField.PARAM_EQUIPMENT_UID, filter.equipmentUid());
        putIfPresent(result, SpecConstant.RentalField.PARAM_CREATED_FROM, filter.from() != null ? boundaryResolver.startOfDay(filter.from()) : null);
        putIfPresent(result, SpecConstant.RentalField.PARAM_CREATED_TO, filter.to() != null ? boundaryResolver.startOfNextDay(filter.to()) : null);
        return result;
    }

    private static void putIfPresent(Map<String, String[]> target, String key, @Nullable String value) {
        if (value != null) {
            target.put(key, new String[]{value});
        }
    }
}
