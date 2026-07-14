package com.github.jenkaby.bikerental.tariff.infrastructure.persistence.mapper;

import com.github.jenkaby.bikerental.shared.mapper.DiscountMapper;
import com.github.jenkaby.bikerental.shared.mapper.DurationMapper;
import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.shared.mapper.QuoteRefMapper;
import com.github.jenkaby.bikerental.tariff.BreakdownCostDetails;
import com.github.jenkaby.bikerental.tariff.DiscountDetail;
import com.github.jenkaby.bikerental.tariff.EquipmentCostBreakdown;
import com.github.jenkaby.bikerental.tariff.EquipmentCostItemV2;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationResult;
import com.github.jenkaby.bikerental.tariff.RentalCostCalculationV2Command;
import com.github.jenkaby.bikerental.tariff.RentalCostQuote;
import com.github.jenkaby.bikerental.tariff.domain.service.BaseRentalCostCalculationResult;
import com.github.jenkaby.bikerental.tariff.domain.service.EquipmentCostBreakdownV2;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.QuoteRequestSnapshot;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.QuoteResultSnapshot;
import com.github.jenkaby.bikerental.tariff.infrastructure.persistence.entity.RentalCostQuoteJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(uses = {MoneyMapper.class, DurationMapper.class, DiscountMapper.class, QuoteRefMapper.class})
public abstract class RentalCostQuoteJpaMapper {

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

    @Mapping(target = "id", source = "quoteId")
    @Mapping(target = "requestSnapshot", source = "inputs")
    @Mapping(target = "resultSnapshot", source = "result")
    @Mapping(target = "consumedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract RentalCostQuoteJpaEntity toEntity(RentalCostQuote quote);

    @Mapping(target = "quoteId", source = "id")
    @Mapping(target = "inputs", source = "requestSnapshot")
    @Mapping(target = "result", source = "resultSnapshot")
    public abstract RentalCostQuote toDomain(RentalCostQuoteJpaEntity entity);

    @Mapping(target = "plannedDurationMinutes", source = "plannedDuration")
    @Mapping(target = "discountPercent", source = "discount")
    abstract QuoteRequestSnapshot toRequestSnapshot(RentalCostCalculationV2Command command);

    @Mapping(target = "plannedDuration", source = "plannedDurationMinutes")
    @Mapping(target = "discount", source = "discountPercent")
    abstract RentalCostCalculationV2Command toCommand(QuoteRequestSnapshot snapshot);

    @Mapping(target = "plannedDurationMinutes", source = "plannedDuration")
    abstract QuoteRequestSnapshot.Item toItemSnapshot(EquipmentCostItemV2 item);

    @Mapping(target = "plannedDuration", source = "plannedDurationMinutes")
    abstract EquipmentCostItemV2 toItem(QuoteRequestSnapshot.Item item);

    abstract QuoteResultSnapshot.Discount toDiscountSnapshot(DiscountDetail discount);

    abstract DiscountDetail toDiscountDetail(QuoteResultSnapshot.Discount discount);

    QuoteResultSnapshot toResultSnapshot(RentalCostCalculationResult result) {
        return new QuoteResultSnapshot(
                toLineSnapshots(result.equipmentBreakdowns()),
                moneyMapper.toBigDecimal(result.subtotal()),
                toDiscountSnapshot(result.discount()),
                moneyMapper.toBigDecimal(result.totalCost()),
                durationMapper.toMinutes(result.effectiveDuration()),
                result.estimate(),
                result.specialPricingApplied());
    }

    RentalCostCalculationResult toResult(QuoteResultSnapshot snapshot) {
        return new BaseRentalCostCalculationResult(
                toBreakdowns(snapshot.equipmentBreakdowns()),
                moneyMapper.toMoney(snapshot.subtotal()),
                toDiscountDetail(snapshot.discount()),
                moneyMapper.toMoney(snapshot.totalCost()),
                durationMapper.toDuration(snapshot.effectiveDurationMinutes()),
                snapshot.estimate(),
                snapshot.specialPricingApplied());
    }

    private List<QuoteResultSnapshot.Line> toLineSnapshots(List<EquipmentCostBreakdown> breakdowns) {
        return breakdowns.stream().map(this::toLineSnapshot).toList();
    }

    private QuoteResultSnapshot.Line toLineSnapshot(EquipmentCostBreakdown breakdown) {
        var detail = breakdown.calculationBreakdown();
        var breakdownSnapshot = new QuoteResultSnapshot.Line.Breakdown(
                detail.getBreakdownPatternCode(),
                detail.getMessage(),
                detail.getParams());
        return new QuoteResultSnapshot.Line(
                breakdown.equipmentId(),
                breakdown.equipmentType(),
                breakdown.tariffId(),
                breakdown.tariffName(),
                breakdown.pricingType(),
                moneyMapper.toBigDecimal(breakdown.itemCost()),
                durationMapper.toMinutes(breakdown.billedDuration()),
                durationMapper.toMinutes(breakdown.overtime()),
                durationMapper.toMinutes(breakdown.forgiven()),
                breakdownSnapshot);
    }

    private List<EquipmentCostBreakdown> toBreakdowns(List<QuoteResultSnapshot.Line> lines) {
        return lines.stream().map(this::toBreakdown).toList();
    }

    private EquipmentCostBreakdown toBreakdown(QuoteResultSnapshot.Line line) {
        var detail = line.calculationBreakdown();
        var rehydrated = new BreakdownCostDetails.Rehydrated(
                detail.breakdownPatternCode(),
                detail.message(),
                detail.params());
        return new EquipmentCostBreakdownV2(
                line.equipmentId(),
                line.equipmentType(),
                line.tariffId(),
                line.tariffName(),
                line.pricingType(),
                moneyMapper.toMoney(line.itemCost()),
                durationMapper.toDuration(line.billedDurationMinutes()),
                durationMapper.toDuration(line.overtimeMinutes()),
                durationMapper.toDuration(line.forgivenMinutes()),
                rehydrated);
    }
}
