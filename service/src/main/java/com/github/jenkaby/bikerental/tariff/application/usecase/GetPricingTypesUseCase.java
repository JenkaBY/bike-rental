package com.github.jenkaby.bikerental.tariff.application.usecase;

import com.github.jenkaby.bikerental.tariff.domain.model.vo.PricingTypeInfo;

import java.util.List;

public interface GetPricingTypesUseCase {
    List<PricingTypeInfo> execute();
}
