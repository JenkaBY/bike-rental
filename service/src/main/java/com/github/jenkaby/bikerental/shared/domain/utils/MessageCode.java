package com.github.jenkaby.bikerental.shared.domain.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageCode {
    public static final String BREAKDOWN_SPECIAL_GROUP = "breakdown.cost.special.group";
    public static final String BREAKDOWN_SPECIAL_PRICE = "breakdown.cost.special";
    public static final String BREAKDOWN_ZERO_COST = "breakdown.cost.special";
    public static final String BREAKDOWN_FLAT_HOURLY_MIN = "breakdown.cost.flat_hourly.minimum";
    public static final String BREAKDOWN_FLAT_HOURLY_STANDARD = "breakdown.cost.flat_hourly.standard";
    public static final String BREAKDOWN_FLAT_HOURLY_MINUTES_ONLY = "breakdown.cost.flat_hourly.minutes_only";
    public static final String BREAKDOWN_DAILY_STANDARD = "breakdown.cost.daily.standard";
    public static final String BREAKDOWN_DAILY_OVERTIME = "breakdown.cost.daily.overtime";
    public static final String BREAKDOWN_FLAT_FEE = "breakdown.cost.flat_fee";
    public static final String BREAKDOWN_DEGRESSIVE_HOURLY_MIN = "breakdown.cost.degressive_hourly.minimum";
    public static final String BREAKDOWN_DEGRESSIVE_HOURLY_STANDARD = "breakdown.cost.degressive_hourly.standard";
    public static final String BREAKDOWN_DEGRESSIVE_HOURLY_MINUTES_ONLY = "breakdown.cost.degressive_hourly.minutes_only";
    //
    public static final String INAPPLICABLE_FIELD = "tariff.validation.inapplicable_field";
    public static final String REQUIRED_FIELD_MISSING = "tariff.validation.required_field_missing";
    public static final String CONSTRAINT_VIOLATION = "tariff.validation.constraint";
}
