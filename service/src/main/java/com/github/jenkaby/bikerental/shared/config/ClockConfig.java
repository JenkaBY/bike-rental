package com.github.jenkaby.bikerental.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;


@Configuration
public class ClockConfig {

    @Bean
    public Clock applicationClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public ZoneId businessZoneId(AppProperties appProperties) {
        return ZoneId.of(appProperties.zoneId());
    }
}
