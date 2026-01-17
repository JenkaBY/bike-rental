package com.github.jenkaby.bikerental.componenttest.config.infra;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers configuration for component tests.
 * Assumes to run in CI/CD pipeline therefore liquibase is enabled.
 */
@Profile("docker")
@TestConfiguration(proxyBeanMethods = false)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=true"
})
public class TestcontainersConfiguration {

    public static PostgreSQLContainer database;

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        database = new PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"));
        return database;
    }

}
