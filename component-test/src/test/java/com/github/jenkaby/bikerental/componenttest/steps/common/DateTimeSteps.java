package com.github.jenkaby.bikerental.componenttest.steps.common;

import com.github.jenkaby.bikerental.componenttest.config.ClockTestConfig;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.*;

@Slf4j
@RequiredArgsConstructor
public class DateTimeSteps {

    private final Clock clock;

    @After("@ResetClock")
    public void resetClock() {
        if (clock instanceof ClockTestConfig.MutableClock mutableClock) {
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
        log.info("Setting current instant to {}", instantString);
        LocalDateTime now = LocalDateTime.parse(instantString);
        log.debug("parsed now: {}", now);
        var currentTime = now.toInstant(ZoneOffset.UTC);
        log.debug("Set instant: {}", currentTime);
        setCurrentTime(currentTime);
    }

    private void setCurrentTime(Instant currentTime) {
        if (clock instanceof ClockTestConfig.MutableClock mutableClock) {
            mutableClock.setInstant(currentTime);
            log.debug("Set project widely the current time: {}", currentTime);
        } else {
            log.warn("Clock is not mutable, cannot set current time to {}", currentTime);
        }
    }
}
