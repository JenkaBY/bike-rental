package com.github.jenkaby.bikerental.componenttest;

import com.github.jenkaby.bikerental.BikeRentalApplication;
import com.github.jenkaby.bikerental.componenttest.config.infra.TestcontainersConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;


@Suite
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.github.jenkaby.bikerental")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @skip")
// uncomment the line below comment out the line above and mark @run the test to be executed
//@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@run")
public class RunComponentTests {


    @SpringBootTest(classes = BikeRentalApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @CucumberContextConfiguration
    @Import(TestcontainersConfiguration.class)
    public static class ApplicationContextTestConfiguration {


    }
}
