package com.github.jenkaby.bikerental.tariff;

import com.github.jenkaby.bikerental.shared.domain.utils.MessageCode;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Getter
@ToString
public abstract class BreakdownCostDetails {
    protected String breakdownPatternCode;
    protected String message;
    protected Object params;

    public BreakdownCostDetails(@NonNull String breakdownPatternCode,
                                @NonNull String message,
                                @Nullable Object params) {
        this.breakdownPatternCode = breakdownPatternCode;
        this.message = message;
        this.params = params;
    }

    public static class Special extends BreakdownCostDetails {
        public Special() {
            super(MessageCode.BREAKDOWN_SPECIAL_PRICE, "Special tariff", null);
        }
    }

    public static class SpecialGroup extends BreakdownCostDetails {
        public SpecialGroup() {
            super(MessageCode.BREAKDOWN_SPECIAL_GROUP, "Special tariff applied to group", null);
        }
    }

    public static class Zero extends BreakdownCostDetails {
        public Zero() {
            super(MessageCode.BREAKDOWN_ZERO_COST, "0 min: 0.00", null);
        }
    }

    public static class FlatHourlyMinCost extends BreakdownCostDetails {
        public FlatHourlyMinCost(String message, Details details) {
            super(MessageCode.BREAKDOWN_FLAT_HOURLY_MIN, message, details);
        }

        public record Details(int durationMinutes, String rate, String surcharge, String total) {
        }
    }

    public static class FlatHourlyStandard extends BreakdownCostDetails {
        public FlatHourlyStandard(String message, Details details) {
            super(MessageCode.BREAKDOWN_FLAT_HOURLY_STANDARD, message, details);
        }

        public record Details(int hours, int minutes, String rate, String total) {
        }
    }

    public static class FlatHourlyMinsOnly extends BreakdownCostDetails {
        public FlatHourlyMinsOnly(String message, Details details) {
            super(MessageCode.BREAKDOWN_FLAT_HOURLY_MINUTES_ONLY, message, details);
        }

        public record Details(int minutes, String total) {
        }
    }

    public static class DailyStandard extends BreakdownCostDetails {
        public DailyStandard(String message, Details details) {
            super(MessageCode.BREAKDOWN_DAILY_STANDARD, message, details);
        }

        public record Details(int days, String total) {
        }
    }

    public static class DailyOvertime extends BreakdownCostDetails {
        public DailyOvertime(String message, Details details) {
            super(MessageCode.BREAKDOWN_DAILY_OVERTIME, message, details);
        }

        public record Details(int days, int hours, int minutes, String total) {
        }
    }

    public static class FlatFee extends BreakdownCostDetails {
        public FlatFee(String message, Details details) {
            super(MessageCode.BREAKDOWN_FLAT_FEE, message, details);
        }

        public record Details(String fee, int days, String total) {
        }
    }

    public static class DegressiveHourlyMin extends BreakdownCostDetails {
        public DegressiveHourlyMin(String message, Details details) {
            super(MessageCode.BREAKDOWN_DEGRESSIVE_HOURLY_MIN, message, details);
        }

        public record Details(int durationMinutes, String rate, String surcharge, String total) {
        }
    }

    public static class DegressiveHourlyStandard extends BreakdownCostDetails {
        public DegressiveHourlyStandard(String message, Details details) {
            super(MessageCode.BREAKDOWN_DEGRESSIVE_HOURLY_STANDARD, message, details);
        }

        public record Details(int hours, int minutes, String rateBreakdown, String total) {
        }
    }

    public static class DegressiveHourlyMinutesOnly extends BreakdownCostDetails {
        public DegressiveHourlyMinutesOnly(String message, Details details) {
            super(MessageCode.BREAKDOWN_DEGRESSIVE_HOURLY_MINUTES_ONLY, message, details);
        }

        public record Details(int minutes, String rateBreakdown, String total) {
        }
    }

    public static class Daily extends BreakdownCostDetails {
        public Daily(String message, Details details) {
            super(MessageCode.BREAKDOWN_DAILY_STANDARD, message, details);
        }

        public record Details(int days, int hours, int minutes, String total) {
        }
    }

    public static class DailyDaysOnly extends BreakdownCostDetails {
        public DailyDaysOnly(String message, Details details) {
            super(MessageCode.BREAKDOWN_DAILY_STANDARD, message, details);
        }

        public record Details(int days, String total) {
        }
    }

    public static class DailyWithOvertime extends BreakdownCostDetails {
        public DailyWithOvertime(String message, Details details) {
            super(MessageCode.BREAKDOWN_DAILY_OVERTIME, message, details);
        }

        public record Details(int days, int hours, int minutes, String total) {
        }
    }
}
