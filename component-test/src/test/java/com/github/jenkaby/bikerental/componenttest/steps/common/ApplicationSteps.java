package com.github.jenkaby.bikerental.componenttest.steps.common;

import io.cucumber.java.en.Given;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@AllArgsConstructor
public class ApplicationSteps {

    private final ApplicationContext applicationContext;

    @Given("application is started")
    public void applicationIsStarted() {
        log.info("Application is started step");
        assertThat(true).as("Application should start").isTrue();
    }
}
