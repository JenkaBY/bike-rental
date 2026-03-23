package com.github.jenkaby.bikerental.shared.mapper;

import com.github.jenkaby.bikerental.shared.domain.model.vo.DiscountPercent;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper
public interface DiscountMapper {

    default DiscountPercent toDiscount(BigDecimal percent) {
        return percent == null ? null : new DiscountPercent(percent);
    }

    default DiscountPercent toDiscount(Integer percent) {
        return percent == null ? null : DiscountPercent.of(percent);
    }

    default BigDecimal toBigDecimal(DiscountPercent discount) {
        return discount == null ? null : discount.percent();
    }
}

