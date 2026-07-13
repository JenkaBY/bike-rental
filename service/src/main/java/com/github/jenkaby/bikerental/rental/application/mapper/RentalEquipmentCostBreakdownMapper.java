package com.github.jenkaby.bikerental.rental.application.mapper;

import com.github.jenkaby.bikerental.rental.domain.model.vo.EquipmentCostResult;
import com.github.jenkaby.bikerental.rental.domain.model.vo.RentalEquipmentCostBreakdown;
import com.github.jenkaby.bikerental.shared.mapper.DurationMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.tariff.EquipmentCostBreakdown;
import com.github.jenkaby.bikerental.tariff.EquipmentCostItemV2;
import com.github.jenkaby.bikerental.tariff.RentalCostQuote;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Mapper(uses = {MoneyMapper.class, DurationMapper.class})
public abstract class RentalEquipmentCostBreakdownMapper {

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

    public RentalEquipmentCostBreakdown toBreakdown(EquipmentCostBreakdown source) {
        var detail = source.calculationBreakdown();
        var calculationDetail = new RentalEquipmentCostBreakdown.CalculationDetail(
                detail.getBreakdownPatternCode(),
                detail.getMessage(),
                detail.getParams()
        );
        return new RentalEquipmentCostBreakdown(
                source.pricingType(),
                source.tariffName(),
                durationMapper.toMinutes(source.billedDuration()),
                durationMapper.toMinutes(source.overtime()),
                durationMapper.toMinutes(source.forgiven()),
                moneyMapper.toBigDecimal(source.itemCost()),
                calculationDetail
        );
    }

    public List<EquipmentCostResult> toFrozenCostResults(int returningEquipmentSize, List<EquipmentCostBreakdown> breakdowns) {
        var results = new ArrayList<EquipmentCostResult>(returningEquipmentSize);
        for (int i = 0; i < returningEquipmentSize; i++) {
            results.add(toCostResult(breakdowns.get(i)));
        }
        return results;
    }

    private EquipmentCostResult toCostResult(EquipmentCostBreakdown input) {
        return new EquipmentCostResult(
                input.equipmentId(),
                input.tariffId(),
                input.itemCost(),
                toBreakdown(input)
        );
    }
}
