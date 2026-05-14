package com.github.jenkaby.bikerental.componenttest.steps.common;

import com.github.jenkaby.bikerental.componenttest.config.ClockTestConfig;
import com.github.jenkaby.bikerental.shared.config.DevClockConfig;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.util.TimeZone;

@Slf4j
@RequiredArgsConstructor
public class DateTimeSteps {

    private final Clock clock;
    private TimeZone defaultTimezone;

    @Before("@UseUTC")
    public void setUTC() {
        this.defaultTimezone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        log.info("Set UTC timezone. Default is {}", this.defaultTimezone);

    }

    @After("@UseUTC")
    public void resetUTC() {
        if (this.defaultTimezone != null) {
            log.info("Reset Timezone to default {}", this.defaultTimezone);
            TimeZone.setDefault(this.defaultTimezone);
        }
    }


    @After("@ResetClock")
    public void resetClock() {
        if (clock instanceof ClockTestConfig.MutableClock mutableClock) {
            mutableClock.reset();
            log.info("Reset the clock to the system time");
        } else if (clock instanceof DevClockConfig.SettableClock mutableClock) {
            mutableClock.reset();
            log.info("Reset the clock to the system time");
        } else {
            log.warn("Clock is not mutable, cannot reset current time");
        }
    }

    @Given("today is {string}")
    public void todayIs(String date) {
        log.info("Setting current date to {}", date);
        LocalDate today = LocalDate.parse(date);
        log.debug("parsed date: {}", today);
        var currentTime = today.atStartOfDay().toInstant(ZoneOffset.UTC);
        log.debug("Set instant: {}", currentTime);
        setCurrentTime(currentTime);
    }

    @Given("now is {string}")
    public void nowIs(String instantString) {
        log.info("Setting current local datetime to {}", instantString);
        LocalDateTime now = LocalDateTime.parse(instantString);
        log.debug("parsed now: {}", now);
        var currentTime = now.atZone(ZoneId.systemDefault());
        log.debug("Set ZDT: {}", currentTime);
        log.debug("Set instant: {}", currentTime.toInstant());
        log.debug("Set local datetime: {}", currentTime.toLocalDateTime());
        setCurrentTime(currentTime.toInstant());
    }

    private void setCurrentTime(Instant currentTime) {
        if (clock instanceof ClockTestConfig.MutableClock mutableClock) {
            mutableClock.setInstant(currentTime);
            log.debug("Set project widely the current time: {}", currentTime);
        } else if (clock instanceof DevClockConfig.SettableClock mutableClock) {
            mutableClock.setInstant(currentTime);
            log.debug("Set project widely the current time: {}", currentTime);

        } else {
            log.warn("Clock is not mutable, cannot set current time to {}", currentTime);
        }
    }
}
