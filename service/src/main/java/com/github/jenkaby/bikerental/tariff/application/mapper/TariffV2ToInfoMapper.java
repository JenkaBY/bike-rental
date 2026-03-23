package com.github.jenkaby.bikerental.tariff.application.mapper;

import com.github.jenkaby.bikerental.tariff.TariffV2Info;
import com.github.jenkaby.bikerental.tariff.domain.model.TariffV2;
import org.mapstruct.Mapper;

@Mapper
public interface TariffV2ToInfoMapper {

    TariffV2Info toTariffV2Info(TariffV2 tariffV2);
}
