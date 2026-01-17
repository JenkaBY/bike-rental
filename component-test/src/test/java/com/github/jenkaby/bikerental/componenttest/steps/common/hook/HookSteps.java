package com.github.jenkaby.bikerental.componenttest.steps.common.hook;


import com.github.jenkaby.bikerental.componenttest.context.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;

import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
@Slf4j
public class HookSteps {

    private final ScenarioContext scenarioContext;

    @BeforeAll
    public static void setUp() {
        speedUpAwaitility();
    }

    @AfterAll
    public static void tearDown() {
        resetAwaitility();
    }

    @After
    public void clearScenarioResources() {
        log.info("Clearing scenario resources...");
        scenarioContext.clearHeaders();
    }

    private static void speedUpAwaitility() {
        Awaitility.setDefaultTimeout(1, TimeUnit.SECONDS);
        Awaitility.setDefaultPollInterval(50, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(30, TimeUnit.MILLISECONDS);
    }

    private static void resetAwaitility() {
        Awaitility.await();
    }
}