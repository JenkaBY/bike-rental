package com.github.jenkaby.bikerental.tariff.application.mapper;

import com.github.jenkaby.bikerental.shared.mapper.MoneyMapper;
import com.github.jenkaby.bikerental.tariff.TariffInfo;
import com.github.jenkaby.bikerental.tariff.domain.model.Tariff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(uses = {MoneyMapper.class})
public interface TariffToInfoMapper {


    @Mapping(target = "basePrice", expression = "java(tariff.getBasePrice().amount())")
    @Mapping(target = "hourPrice", expression = "java(tariff.getHourPrice() != null ? tariff.getHourPrice().amount() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "dayPrice", expression = "java(tariff.getDayPrice() != null ? tariff.getDayPrice().amount() : java.math.BigDecimal.ZERO)")
    @Mapping(target = "active", expression = "java(tariff.isActive())")
    TariffInfo toTariffInfo(Tariff tariff);
}
