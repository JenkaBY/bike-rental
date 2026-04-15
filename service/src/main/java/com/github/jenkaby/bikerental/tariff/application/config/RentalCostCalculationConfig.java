package com.github.jenkaby.bikerental.tariff.application.config;

import com.github.jenkaby.bikerental.shared.application.service.MessageService;
import com.github.jenkaby.bikerental.shared.config.RentalProperties;
import com.github.jenkaby.bikerental.tariff.application.strategy.ForgivenessStrategy;
import com.github.jenkaby.bikerental.tariff.application.strategy.ThresholdForgivenessStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class RentalCostCalculationConfig {

    @Bean
    ForgivenessStrategy forgivenessStrategy(
            RentalProperties rentalProperties,
            MessageService messageService) {
        return new ThresholdForgivenessStrategy(
                rentalProperties.forgiveness(),
                messageService
        );
    }
}
