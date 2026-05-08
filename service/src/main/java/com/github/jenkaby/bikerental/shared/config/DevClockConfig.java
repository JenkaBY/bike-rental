package com.github.jenkaby.bikerental.shared.config;

import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

@Profile({"dev", "test"})
@Configuration
public class DevClockConfig {

    @Primary
    @Bean
    public SettableClock clock(Clock applicationClock) {
        return new SettableClock(applicationClock);
    }

    public static final class SettableClock extends Clock {

        @Setter
        private @Nullable Instant fixedInstant;
        private @Nullable Clock offsetClock;
        private final Clock delegate;

        public SettableClock(Clock original) {
            this.delegate = original;
        }

        public void setInstant(@NonNull Instant desiredInstant) {
            this.fixedInstant = desiredInstant;
            this.offsetClock = Clock.offset(delegate, Duration.between(delegate.instant(), desiredInstant));
        }

        public void reset() {
            this.fixedInstant = null;
            this.offsetClock = null;
        }

        @Override
        public ZoneId getZone() {
            return getClock().getZone();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            SettableClock mutableClock = new SettableClock(getClock().withZone(zone));
            mutableClock.setFixedInstant(fixedInstant);
            return mutableClock;
        }

        @Override
        public Instant instant() {
            return getClock().instant();
        }

        public boolean isFixed() {
            return this.offsetClock != null;
        }

        private Clock getClock() {
            return offsetClock != null ? offsetClock : delegate;
        }
    }
}
