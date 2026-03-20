package com.github.jenkaby.bikerental.tariff.web.query.mapper;

import com.github.jenkaby.bikerental.shared.mapper.DiscountMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.tariff.DiscountDetail;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationResponse;
import org.mapstruct.Mapper;

@Mapper(uses = {DiscountMapper.class, MoneyMapper.class})
public interface DiscountDetailMapper {

    CostCalculationResponse.DiscountDetailResponse toResponse(DiscountDetail source);
}
