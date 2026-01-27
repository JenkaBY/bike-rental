package com.github.jenkaby.bikerental;

import com.github.jenkaby.bikerental.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class BikeRentalApplicationTest {

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        String activeProfiles = System.getProperty("spring.profiles.active", "");
        if (activeProfiles.contains("docker")) {
//              Assumes to run in CI/CD pipeline therefore liquibase is enabled.
            registry.add("spring.liquibase.enabled", () -> true);
        }
    }

    @Test
    void contextLoads() {
        assertThat(true)
                .as("Spring context is initialized and loaded successfully. It ensures that the app started with infrastructure components.")
                .isTrue();
    }

}
