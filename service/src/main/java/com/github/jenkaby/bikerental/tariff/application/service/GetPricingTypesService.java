package com.github.jenkaby.bikerental.tariff.application.service;

import com.github.jenkaby.bikerental.shared.application.service.MessageService;
import com.github.jenkaby.bikerental.tariff.application.usecase.GetPricingTypesUseCase;
import com.github.jenkaby.bikerental.tariff.domain.model.PricingType;
import com.github.jenkaby.bikerental.tariff.domain.model.vo.PricingTypeInfo;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
class GetPricingTypesService implements GetPricingTypesUseCase {

    private final MessageService messageService;

    GetPricingTypesService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public List<PricingTypeInfo> execute() {
        Locale locale = LocaleContextHolder.getLocale();
        return Arrays.stream(PricingType.values())
                .map(type -> new PricingTypeInfo(
                        type.name(),
                        messageService.getMessage(type.getCodeTitle(), locale),
                        messageService.getMessage(type.getCodeDescription(), locale)
                ))
                .collect(Collectors.toList());
    }
}
