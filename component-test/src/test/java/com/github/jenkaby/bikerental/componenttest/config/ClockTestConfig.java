package com.github.jenkaby.bikerental.componenttest.config;

import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

@Configuration(proxyBeanMethods = false)
public class ClockTestConfig {

    @Bean
    @Primary
    public Clock mutableClock(Clock applicationClock) {
        return new MutableClock(applicationClock);
    }

    // Not ThreadSafe, but sufficient for single-threaded component tests
    public static class MutableClock extends Clock {
        @Setter
        private @Nullable Instant fixedInstant;
        private @Nullable Clock offsetClock;
        private final Clock delegate;

        public MutableClock(Clock original) {
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
            MutableClock mutableClock = new MutableClock(getClock().withZone(zone));
            mutableClock.setFixedInstant(fixedInstant);
            return mutableClock;
        }

        @Override
        public Instant instant() {
            return getClock().instant();
        }

        private Clock getClock() {
            return offsetClock != null ? offsetClock : delegate;
        }
    }
}
