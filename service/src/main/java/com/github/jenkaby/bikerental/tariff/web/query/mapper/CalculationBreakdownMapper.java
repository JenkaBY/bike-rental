package com.github.jenkaby.bikerental.tariff.web.query.mapper;

import com.github.jenkaby.bikerental.shared.mapper.DurationMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.tariff.EquipmentCostBreakdown;
import com.github.jenkaby.bikerental.tariff.web.query.dto.CostCalculationResponse;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public abstract class CalculationBreakdownMapper {

    private MoneyMapper moneyMapper;
    private DurationMapper durationMapper;

    @Autowired
    public void setMoneyMapper(MoneyMapper moneyMapper) {
        this.moneyMapper = moneyMapper;
    }

    @Autowired
    public void setDurationMapper(DurationMapper durationMapper) {
        this.durationMapper = durationMapper;
    }

    public List<CostCalculationResponse.EquipmentCostBreakdownResponse> toBreakdownResponses(List<EquipmentCostBreakdown> source) {
        if (source == null) {
            return List.of();
        }
        return source.stream()
                .map(this::toBreakdownResponse)
                .toList();
    }

    public CostCalculationResponse.EquipmentCostBreakdownResponse toBreakdownResponse(EquipmentCostBreakdown source) {
        return new CostCalculationResponse.EquipmentCostBreakdownResponse(
                source.equipmentType(),
                source.tariffId(),
                source.tariffName(),
                source.pricingType(),
                moneyMapper.toBigDecimal(source.itemCost()),
                durationMapper.toMinutes(source.billedDuration()),
                durationMapper.toMinutes(source.overtime()),
                durationMapper.toMinutes(source.forgiven()),
                source.calculationBreakdown()
        );
    }
}
