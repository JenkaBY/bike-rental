package com.github.jenkaby.bikerental;

import com.github.jenkaby.bikerental.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class BikeRentalApplicationTest {

    @Test
    void contextLoads() {
        assertThat(true)
                .as("Spring context is initialized and loaded successfully. It ensures that the app started with infrastructure components.")
                .isTrue();
    }

}
